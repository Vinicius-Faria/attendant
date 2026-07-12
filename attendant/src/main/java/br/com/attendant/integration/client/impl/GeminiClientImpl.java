package br.com.attendant.integration.client.impl;

import br.com.attendant.config.BusinessException;
import br.com.attendant.config.ExceptionEnum;
import br.com.attendant.dto.ContextDto;
import br.com.attendant.entity.ChatMessage;
import br.com.attendant.entity.ChatSession;
import br.com.attendant.entity.MessageRole;
import br.com.attendant.integration.GeminiToolRegistry;
import br.com.attendant.integration.client.GeminiClient;
import br.com.attendant.integration.config.GeminiProperties;
import br.com.attendant.integration.context.GeminiToolContext;
import br.com.attendant.integration.model.ModelGemini;
import br.com.attendant.integration.strategy.GeminiToolStrategy;
import br.com.attendant.service.ChatMessageService;
import com.google.genai.Client;
import com.google.genai.types.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Supplier;

@Service
class GeminiClientImpl implements GeminiClient {

    private final Client client;
    private final GeminiToolRegistry toolRegistry;
    private final ChatMessageService chatMessageService;
    private final String modelName;

    GeminiClientImpl(
            GeminiProperties geminiProperties,
            GeminiToolRegistry toolRegistry,
            ChatMessageService chatMessageService
    ) {
        this.client = Client.builder().apiKey(geminiProperties.getApiKey()).build();
        this.toolRegistry = toolRegistry;
        this.chatMessageService = chatMessageService;
        this.modelName = ModelGemini.GEMINI_2_5_FLASH.getModel();
    }

    @Override
    public String messageByWhatsApp(ContextDto context) {
        try {
            ChatSession session = context.getChatSession();
            List<ChatMessage> historicoPlanificado = context.getChatMessageList();
            List<Content> contents = converteHistoricoParaGemini(historicoPlanificado);

            GenerateContentConfig config = GenerateContentConfig.builder()
                    .systemInstruction(buildSystemInstructionContent(context))
                    .tools(toolRegistry.getAllTools())
                    .build();

            GenerateContentResponse response = executeWithRetry(() ->
                    client.models.generateContent(modelName, contents, config)
            );

            Optional<Part> functionCallPartOpt = obterPrimeiraFunctionCall(response);

            while (functionCallPartOpt.isPresent()) {
                Part functionCallPart = functionCallPartOpt.get();
                FunctionCall functionCall = functionCallPart.functionCall().get();

                String functionName = functionCall.name().orElse("");
                Map<String, Object> args = functionCall.args().orElse(Collections.emptyMap());
                GeminiToolStrategy strategy = toolRegistry.getStrategy(functionName);

                if (strategy == null) {
                    break;
                }

                String jsonResultadoDaTool = strategy.execute(args, buildToolContext(session));

                contents.add(Content.builder()
                        .role("model")
                        .parts(List.of(functionCallPart))
                        .build());

                FunctionResponse functionResponse = FunctionResponse.builder()
                        .name(functionName)
                        .response(Map.of("result", jsonResultadoDaTool))
                        .build();

                contents.add(Content.builder()
                        .role("tool")
                        .parts(List.of(Part.builder().functionResponse(functionResponse).build()))
                        .build());

                response = executeWithRetry(() ->
                        client.models.generateContent(modelName, contents, config)
                );

                functionCallPartOpt = obterPrimeiraFunctionCall(response);
            }

            String respostaFinal = response.text();
            if (respostaFinal != null && !respostaFinal.isEmpty()) {
                salvarMensagemModel(session, respostaFinal);
            }

            return respostaFinal;

        } catch (BusinessException be) {
            throw be;
        } catch (Exception e) {
            System.err.println("[Gemini Integration Error]: " + e.getMessage());
            e.printStackTrace();
            throw new BusinessException(ExceptionEnum.GENERIC, "Falha na comunicação inteligente com o provedor.");
        }
    }

    @Override
    public String generateText(String prompt) {
        try {
            GenerateContentResponse response = executeWithRetry(() ->
                    client.models.generateContent(modelName, prompt, null)
            );
            return response.text();
        } catch (Exception e) {
            System.err.println("[Gemini Simple Text Error]: " + e.getMessage());
            throw new BusinessException(ExceptionEnum.GENERIC, ExceptionEnum.GENERIC.getDescricao());
        }
    }

    /**
     * Envolve a execução contra a API em um bloco de até 3 tentativas
     */
    private GenerateContentResponse executeWithRetry(Supplier<GenerateContentResponse> apiCall) {
        int maxAttempts = 3;
        Exception lastException = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return apiCall.get();
            } catch (Exception e) {
                lastException = e;
//                System.out.format("[Gemini SDK Warning] Tentativa %d de %d falhou: %s\n", attempt, maxAttempts, e.getMessage());

                if (attempt < maxAttempts) {
                    try {
                        Thread.sleep(1000L * attempt);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new BusinessException(ExceptionEnum.GENERIC, "Interrupção durante reprocessamento.");
                    }
                }
            }
        }
        throw new BusinessException(ExceptionEnum.GENERIC, "Serviço temporariamente indisponível. Motivo: " + lastException.getMessage());
    }

    private Optional<Part> obterPrimeiraFunctionCall(GenerateContentResponse response) {
        return response.candidates().stream()
                .flatMap(Collection::stream)
                .map(Candidate::content)
                .flatMap(Optional::stream)
                .map(Content::parts)
                .flatMap(Optional::stream)
                .flatMap(Collection::stream)
                .filter(part -> part.functionCall().isPresent())
                .findFirst();
    }

    private GeminiToolContext buildToolContext(ChatSession session) {
        Long enterpriseId = (session.getEnterprise() != null) ? session.getEnterprise().getId() : null;
        return new GeminiToolContext(enterpriseId, session);
    }

    private List<Content> converteHistoricoParaGemini(List<ChatMessage> historico) {
        List<Content> geminiContents = new ArrayList<>();

        for (ChatMessage msg : historico) {
            if (MessageRole.MODEL.equals(msg.getRole()) && (msg.getContent() == null || msg.getContent().isBlank())) {
                continue;
            }

            String role = msg.getRole().name().toLowerCase();
            if (!"user".equals(role) && !"model".equals(role)) {
                role = "user";
            }

            geminiContents.add(Content.builder()
                    .role(role)
                    .parts(List.of(Part.builder().text(msg.getContent()).build()))
                    .build());
        }

        return geminiContents;
    }

    private Content buildSystemInstructionContent(ContextDto context) {
        return Content.builder()
                .parts(List.of(Part.builder().text(buildSystemInstruction(context)).build()))
                .build();
    }

    private String buildSystemInstruction(ContextDto context) {
        String dataHoraAtual = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy 'às' HH:mm", new Locale("pt", "BR")));

        StringBuilder blocoAgenda = new StringBuilder();
        if (context.getAgenda() != null) {
            blocoAgenda.append("""
                ATENÇÃO CRÍTICA (AGENDAMENTO ATIVO ENCONTRADO):
                O cliente JÁ POSSUI uma reserva confirmada no sistema com os seguintes dados:
                - %s
                DIRETRIZES DE MODIFICAÇÃO:
                1. Se o cliente quiser CANCELAR, use imediatamente a ferramenta 'cancelar_servico_agendado' (ela não precisa de parâmetros).
                2. Se o cliente quiser REMARCAR/ALTERAR o horário ou dia, descubra o novo momento desejado e use a ferramenta 'atualizar_horario_agendado' passando o parâmetro 'novaDataHoraISO'.
                3. NÃO faça buscas de novos horários do zero se a intenção dele for mexer no agendamento que ele já tem.
                """.formatted(context.getAgenda().toString()));
        } else {
            blocoAgenda.append("""
                STATUS DO CLIENTE: O cliente NÃO possui nenhum agendamento ativo no momento.
                Siga o fluxo padrão de identificar o serviço, oferecer horários e criar uma nova reserva.
                """);
        }

        return """
            Você é um atendente virtual profissional de um estabelecimento de estética/beleza no WhatsApp.
            Sua missão é acolher o cliente com simpatia, naturalidade, agilidade e foco na resolução rápida.
            
            DIRETRIZES DE PERSONALIDADE E TOM DE VOZ:
            1. NUNCA use formatação de data técnica como "2026-06-21" ou termos robóticos como "ID do serviço".
            2. Converta datas técnicas para o dia a dia: "amanhã", "depois de amanhã", "segunda-feira", ou formato brasileiro (ex: 21/06).
            3. Interprete expressões naturais de tempo (ex: "próxima segunda", "este sábado", "domingo agora").
            4. Seja conciso. Use quebras de linha e emojis moderadamente (✂️, 💈, 👍) para deixar a conversa leve.
            
            REGRA CRÍTICA DE RESPOSTA (SEM ENROLAÇÃO):
            1. NUNCA envie mensagens de transição ou espera como "Só um instantinho...", "Espere um pouco...". Execute as ferramentas em silêncio e responda apenas com o resultado final.
            2. Toda resposta sua deve ser conclusiva: ou entrega a informação pronta ou faz uma pergunta direta que avance o atendimento.
            
            REGRAS DE AGENDAMENTO E CONSULTA:
            1. Quando o cliente quiser agendar ou consultar horários, você DEVE usar a ferramenta "consultar_horarios_disponiveis".
            2. Nunca invente horários. O resultado da ferramenta é a única fonte da verdade.
            
            TRATAMENTO DE DIAS FECHADOS OU SEM VAGAS:
            1. Se a ferramenta retornar que está fechado ou sem vagas, sugira amigavelmente o próximo dia útil.
            
            MEMÓRIA DE CONTEXTO E INTENÇÃO:
            1. Analise sempre as últimas mensagens da conversa. Se o cliente combinou uma data e na mensagem seguinte diz apenas "15h", assuma que é 15h daquela data.
            
            RECURSO CRÍTICO: FLUXO E APRESENTAÇÃO DE HORÁRIOS (PROIBIDO POLUIR O CHAT):
            1. NUNCA envie uma lista longa de horários. Textos gigantescos com dezenas de opções são horríveis no WhatsApp e afastam o cliente.
            2. Se a ferramenta de consulta retornar muitos horários disponíveis, você deve selecionar e exibir NO MÁXIMO as 3 melhores opções mais próximas do que o cliente demonstrou interesse (ex: focar no período da manhã ou da tarde).
            3. Se o cliente não informar o período, pergunte se prefere manhã, tarde ou noite antes de sugerir qualquer horário.
            4. Só liste mais do que 3 horários se o cliente pedir explicitamente (ex: "Me manda todos os horários que você tem na tarde").
            
            ORDEM DE PRIORIDADE DE EXIBIÇÃO:
            Horário exato solicitado -> Horários mais próximos -> No máximo 3 opções do período desejado.
            
            REGRAS PARA EFETIVAR NOVO AGENDAMENTO:
            1. É PROIBIDO adivinhar o "serviceId". Execute a ferramenta de consulta de serviços em silêncio para descobrir o ID real.
            2. Você precisa de 3 informações antes de chamar "realizar_agendamento": ID do Serviço, Dia/Horário confirmado e Nome do cliente. Falta o nome? Peça de forma natural.
            3. Assim que coletar o nome, invoque IMEDIATAMENTE a ferramenta "realizar_agendamento".
            4. Jamais salve um novo agendamento com o Nome: [Nome do Cliente] ou algo parecido. Se não tem todas as informações, busque as com forma natural com o cliente.
            
            %s
            CONTEXTO DE TEMPO ATUAL:
            Hoje é %s. Use isso de base para calcular "amanhã", "este sábado", etc.
            """.formatted(blocoAgenda.toString(), dataHoraAtual);
    }

    private void salvarMensagemModel(ChatSession session, String conteudo) {
        ChatMessage respostaModel = new ChatMessage();
        respostaModel.setCreatedAt(LocalDateTime.now());
        respostaModel.setRole(MessageRole.MODEL);
        respostaModel.setContent(conteudo);
        respostaModel.setSession(session);

        chatMessageService.save(respostaModel);
    }
}