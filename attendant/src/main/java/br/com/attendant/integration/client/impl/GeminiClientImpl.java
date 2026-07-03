package br.com.attendant.integration.client.impl;

import br.com.attendant.config.BusinessException;
import br.com.attendant.config.ExceptionEnum;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
class GeminiClientImpl implements GeminiClient {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final Client client;
    private final GeminiToolRegistry toolRegistry;
    private final ChatMessageService chatMessageService;

    GeminiClientImpl(
            GeminiProperties geminiProperties,
            GeminiToolRegistry toolRegistry,
            ChatMessageService chatMessageService
    ) {
        this.client = Client.builder().apiKey(geminiProperties.getApiKey()).build();
        this.toolRegistry = toolRegistry;
        this.chatMessageService = chatMessageService;
    }

    @Override
    public String messageByWhatsApp(ChatSession session, List<ChatMessage> historicoPlanificado) {
        try {
            List<Content> contents = converteHistoricoParaGemini(historicoPlanificado);
            GenerateContentConfig config = GenerateContentConfig.builder()
                    .systemInstruction(buildSystemInstructionContent())
                    .tools(toolRegistry.getAllTools())
                    .build();

            GenerateContentResponse response = client.models.generateContent(
                    ModelGemini.GEMINI_2_5_FLASH.getModel(),
                    contents,
                    config
            );

            Optional<Part> functionCallPartOpt = obterPrimeiraFunctionCall(response);

            while (functionCallPartOpt.isPresent()) {
                Part functionCallPart = functionCallPartOpt.get();
                FunctionCall functionCall = functionCallPart.functionCall().get();

                String functionName = functionCall.name().orElse("");
                Map<String, Object> args = functionCall.args().orElse(Collections.emptyMap());
                GeminiToolStrategy strategy = toolRegistry.getStrategy(functionName);

                if (strategy == null) break;

                String jsonResultadoDaTool = strategy.execute(args, buildToolContext(session));

                Content chamadaDoModelo = Content.builder()
                        .role("model")
                        .parts(List.of(functionCallPart))
                        .build();
                contents.add(chamadaDoModelo);

                FunctionResponse functionResponse = FunctionResponse.builder()
                        .name(functionName)
                        .response(Map.of("result", jsonResultadoDaTool))
                        .build();

                Content respostaDaTool = Content.builder()
                        .role("tool")
                        .parts(List.of(Part.builder().functionResponse(functionResponse).build()))
                        .build();
                contents.add(respostaDaTool);

                response = client.models.generateContent(
                        ModelGemini.GEMINI_2_5_FLASH.getModel(),
                        contents,
                        config
                );

                functionCallPartOpt = obterPrimeiraFunctionCall(response);
            }

            String respostaFinal = response.text();
            if (respostaFinal != null && !respostaFinal.isEmpty()) {
                salvarMensagemModel(session, respostaFinal);
            }

            return respostaFinal;

        } catch (Exception e) {
            throw new BusinessException(ExceptionEnum.GENERIC, ExceptionEnum.GENERIC.getDescricao());
        }
    }

    @Override
    public String generateText(String prompt) {
        try {
            GenerateContentResponse response = client.models.generateContent(
                    ModelGemini.GEMINI_2_5_FLASH.getModel(),
                    prompt,
                    null
            );
            return response.text();
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(ExceptionEnum.GENERIC, ExceptionEnum.GENERIC.getDescricao());
        }
    }

    /**
     * Auxiliar modificado para apenas extrair o pedaço (Part) contendo a intenção de FunctionCall.
     */
    private Optional<Part> obterPrimeiraFunctionCall(GenerateContentResponse response) {
        Optional<List<Candidate>> candidates = response.candidates();
        if (candidates.isEmpty() || candidates.get().isEmpty()) {
            return Optional.empty();
        }

        Optional<Content> content = candidates.get().get(0).content();
        if (content.isEmpty()) {
            return Optional.empty();
        }

        Optional<List<Part>> parts = content.get().parts();
        if (parts.isEmpty()) {
            return Optional.empty();
        }

        for (Part part : parts.get()) {
            if (part.functionCall().isPresent()) {
                return Optional.of(part);
            }
        }

        return Optional.empty();
    }

    private GeminiToolContext buildToolContext(ChatSession session) {
        Long enterpriseId = null;
        if (session.getEnterprise() != null) {
            enterpriseId = session.getEnterprise().getId();
        }
        return new GeminiToolContext(enterpriseId, session);
    }

    private List<Content> converteHistoricoParaGemini(List<ChatMessage> historico) {
        List<Content> geminiContents = new ArrayList<>();

        for (ChatMessage msg : historico) {
            if (MessageRole.MODEL.equals(msg.getRole()) && (msg.getContent() == null || msg.getContent().isBlank())) {
                continue;
            }

            String role = msg.getRole().name().toLowerCase();
            if (!role.equals("user") && !role.equals("model")) {
                role = "user";
            }

            Content content = Content.builder()
                    .role(role)
                    .parts(List.of(Part.builder().text(msg.getContent()).build()))
                    .build();

            geminiContents.add(content);
        }

        return geminiContents;
    }

    private Content buildSystemInstructionContent() {
        return Content.builder()
                .parts(List.of(Part.builder().text(buildSystemInstruction()).build()))
                .build();
    }

    private String buildSystemInstruction() {
        String dataHoraAtual = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm"));

        return """
        Você é um atendente virtual profissional de uma barbearia no WhatsApp.
        Sua missão é acolher o cliente com simpatia, naturalidade, agilidade e foco na resolução.
        
        DIRETRIZES DE PERSONALIDADE E TOM DE VOZ:
        1. NUNCA use formatação de data técnica como "2026-06-21" ou termos robóticos como "não encontrei horários cadastrados".
        2. Converta datas técnicas para o dia a dia: "amanhã", "depois de amanhã", "segunda-feira", ou formato brasileiro (ex: 21/06).
        3. Interprete expressões naturais de tempo (ex: "próxima segunda", "este sábado", "domingo agora").
        4. Seja conciso. Use quebras de linha e emojis moderadamente (✂️, 💈, 👍) para deixar a conversa leve.
        
        ⚠️ REGRA CRÍTICA DE RESPOSTA (SEM ENROLAÇÃO):
        1. NUNCA envie mensagens de transição ou espera como "Só um instantinho...", "Espere um pouco...", "Vou dar uma olhada e já volto".
        2. O cliente nunca deve ver o processo de pensamento ou espera. Execute as ferramentas em silêncio e responda APENAS quando tiver o resultado final em mãos.
        3. Toda resposta sua deve ser conclusiva: ou entrega a informação pronta (horários disponíveis) ou faz uma pergunta direta que avance o agendamento.
        
        REGRAS DE AGENDAMENTO E CONSULTA:
        1. Quando o cliente quiser agendar ou consultar horários, você DEVE usar a ferramenta "consultar_horarios_disponiveis".
        2. Nunca invente horários ou disponibilidade. O resultado da ferramenta é a única fonte da verdade.
        
        TRATAMENTO DE DIAS FECHADOS OU SEM VAGAS:
        1. Se a ferramenta retornar "FECHADO" ou "SEM_VAGAS": explique de forma amigável e sugira automaticamente o próximo dia útil disponível.
           Exemplo: "Domingo a gente não abre. Mas posso dar uma olhada na segunda para você, o que acha?"
           Exemplo: "Esse dia já está lotado. Quer que eu veja os horários do dia seguinte para você?"
        
        MEMÓRIA DE CONTEXTO E INTENÇÃO:
        1. Analise sempre as últimas mensagens da conversa. Considere informações fornecidas anteriormente (como o dia já combinado) para não fazer perguntas redundantes.
        2. Se o cliente acabou de combinar uma data e na mensagem seguinte diz apenas "15h", assuma que é 15h daquela data.
        
        FLUXO E APRESENTAÇÃO DE HORÁRIOS:
        1. Não exponha a agenda completa sem necessidade. Mostre no máximo 2 ou 3 opções por vez, focando no período desejado.
        2. Se o cliente não informar o período: pergunte se prefere manhã, tarde ou noite.
        3. Se o cliente pedir um horário específico e ele estiver disponível, ofereça apenas ele. Se não estiver, sugira os mais próximos.
        4. Só liste vários horários se o cliente pedir explicitamente (ex: "Me manda a lista de horários").
        
        ORDEM DE PRIORIDADE:
        1. Horário exato solicitado -> Horários próximos -> Período desejado -> Sugestões alternativas.
        
        REGRAS PARA EFETIVAR O AGENDAMENTO (CADEIA DE FERRAMENTAS):
        1. Você NUNCA possui IDs de serviços na memória. É PROIBIDO adivinhar o "serviceId". Execute a ferramenta de consulta de serviços em silêncio para descobrir o ID real.
        2. Você NUNCA deve chamar a ferramenta "realizar_agendamento" antes de ter as 3 informações fundamentais:
           - O ID do Serviço (retornado pela ferramenta de serviços).
           - O Dia e Horário desejado (confirmado disponível).
           - O Nome do cliente.
        3. Se você tem o serviço e o horário, mas falta o nome, peça o nome de forma natural: "Show! Qual é o seu nome para eu colocar aqui no agendamento?"
        4. Assim que coletar o nome, invoque IMEDIATAMENTE a ferramenta "realizar_agendamento".
        5. Resposta de Sucesso: Quando a ferramenta retornar "SUCESSO", confirme de forma calorosa, resumindo os dados (Serviço, Dia, Horário e Nome).
        
        CONTEXTO DE TEMPO ATUAL:
        Hoje é %s. Use isso de base para calcular "amanhã", "próxima segunda", etc.
        """.formatted(dataHoraAtual);
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