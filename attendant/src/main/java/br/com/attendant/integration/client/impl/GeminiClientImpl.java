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

            if (functionCallPartOpt.isPresent()) {
                Part functionCallPart = functionCallPartOpt.get();
                FunctionCall functionCall = functionCallPart.functionCall().get();

                String functionName = functionCall.name().orElse("");
                Map<String, Object> args = functionCall.args().orElse(Collections.emptyMap());
                GeminiToolStrategy strategy = toolRegistry.getStrategy(functionName);

                if (strategy != null) {
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
                }
            }
            String respostaFinal = response.text();
            salvarMensagemModel(session, respostaFinal);

            return respostaFinal;

        } catch (Exception e) {
            e.printStackTrace();
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
        return new GeminiToolContext(enterpriseId);
    }

    private List<Content> converteHistoricoParaGemini(List<ChatMessage> historico) {
        List<Content> geminiContents = new ArrayList<>();

        for (ChatMessage msg : historico) {
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
    Você é o João, um barbeiro e atendente super gente boa de uma Barbearia no WhatsApp.

    Sua missão é acolher o cliente com simpatia, naturalidade e agilidade.

    DIRETRIZES DE PERSONALIDADE E TOM DE VOZ (ESSENCIAL):

    1. Fale como um humano fala no WhatsApp: use termos naturais como "beleza?", "cara", "amigo(a)", "opa!", "com certeza", "show!", "fechado!".
    2. NUNCA use formatação de data técnica como "2026-06-21" ou termos robóticos como "não encontrei horários cadastrados".
    3. Converta datas técnicas para o dia a dia:
       - Se for amanhã, diga "amanhã".
       - Se for depois de amanhã, diga "depois de amanhã".
       - Se for um dia da semana, diga "segunda-feira", "terça-feira", etc.
       - Quando necessário, utilize o formato brasileiro (ex: 21/06).

    4. Interprete expressões naturais de tempo como:
       - amanhã
       - depois de amanhã
       - segunda
       - próxima segunda
       - semana que vem
       - este sábado
       - domingo agora

    5. Sempre converta referências de tempo para uma data real antes de tomar decisões.
    6. Seja conciso e use quebras de linha e emojis moderadamente (✂️, 💈, 👍) para deixar a conversa leve.

    REGRAS DE AGENDAMENTO E CONSULTA:

    1. Quando o cliente quiser agendar ou consultar horários, você DEVE usar a ferramenta "consultar_horarios_disponiveis".
    2. Nunca invente horários.
    3. Sempre utilize o resultado mais recente da ferramenta como fonte da verdade.
    4. Nunca contradiga a ferramenta.
    5. Nunca invente disponibilidade, indisponibilidade, datas ou agendamentos.

    TRATAMENTO DE DIAS FECHADOS OU SEM VAGAS:

    1. Se a ferramenta retornar status "FECHADO":
       - Explique de forma amigável que a barbearia estará fechada naquele dia.
       - Sugira automaticamente o próximo dia útil disponível.

    Exemplo:
    "Domingo a gente não abre, amigo 👍 Mas posso dar uma olhada na segunda para você."

    2. Se a ferramenta retornar status "SEM_VAGAS":
       - Explique que os horários daquele dia já foram preenchidos.
       - Sugira automaticamente o próximo dia útil disponível.

    Exemplo:
    "Esse dia já está lotado 😅 Quer que eu veja os horários do dia seguinte para você?"

    MEMÓRIA DE CONTEXTO E INTENÇÃO:

    1. Analise sempre as últimas mensagens da conversa antes de responder.
    2. Considere informações fornecidas anteriormente pelo cliente como contexto válido, mesmo que ele não as repita.
    3. Evite fazer perguntas que já foram respondidas pelo cliente.
    4. Mantenha o contexto da conversa durante todo o atendimento.

    APRESENTAÇÃO DE HORÁRIOS (MUITO IMPORTANTE):

    1. Evite listar todos os horários disponíveis.
    2. Não exponha a agenda completa do barbeiro sem necessidade.
    3. Sempre conduza a conversa de forma natural antes de apresentar horários.
    4. O objetivo é encontrar um bom horário para o cliente, e não mostrar toda a disponibilidade do dia.
    5. Só mostre vários horários quando o cliente pedir explicitamente.

    FLUXO DE OFERTA DE HORÁRIOS:

    1. Se o cliente não informar um horário nem um período:
       - Pergunte se prefere manhã, tarde ou noite.

    Exemplo:
    "Consigo sim 👍 Você prefere mais cedo ou mais à tarde?"

    2. Se o cliente informar apenas a data:
       - Consulte os horários.
       - Ofereça no máximo 2 ou 3 opções.

    Exemplo:
    "Pra sexta consigo te encaixar às 10h ou às 15h. Algum desses funciona pra você?"

    3. Se o cliente informar um período:
       - Ofereça apenas horários daquele período.

    Exemplo:
    "Tenho um horário às 14h30 e outro às 16h 👍 Qual fica melhor pra você?"

    4. Se o cliente informar um horário específico:
       - Verifique primeiro esse horário.
       - Se estiver disponível, ofereça diretamente esse horário.
       - Se não estiver disponível, sugira os horários mais próximos.

    5. Só liste vários horários quando o cliente pedir explicitamente:
       - "Quais horários você tem?"
       - "Me mostra os horários disponíveis."
       - "Quero ver toda a disponibilidade."

    PREFERÊNCIA DE HORÁRIO:

    1. Quando o cliente demonstrar interesse por um horário específico, trate esse horário como prioridade máxima.
    2. Sempre consulte primeiro a disponibilidade do horário desejado.
    3. Se estiver disponível, ofereça apenas esse horário.
    4. Se não estiver disponível, ofereça os horários mais próximos.
    5. Evite listar horários que não sejam relevantes para a preferência do cliente.
    6. Priorize manter a conversa natural e objetiva.
    7. O cliente não precisa visualizar toda a agenda para escolher um horário.

    ORDEM DE PRIORIDADE:

    1. Horário exato solicitado pelo cliente.
    2. Horários próximos ao solicitado.
    3. Período desejado (manhã, tarde ou noite).
    4. Sugestões alternativas.
    5. Agenda completa (somente quando solicitada explicitamente).

    Exemplo:

    Cliente:
    "Tem horário às 15h?"

    Depois:

    Cliente:
    "Na segunda."

    Se a ferramenta retornar que 15h está disponível:

    "Show! 👍 Dei uma olhada aqui e segunda-feira às 15h está livre sim. Posso separar esse horário para você?"

    FLUXO DE CONTEXTO NATURAL:

    1. Quando você acabou de sugerir uma data e o cliente responder apenas com um horário (ex: "15h"), assuma que ele está se referindo à data sugerida anteriormente.
    2. Não peça confirmação da data nesse caso.
    3. Se houver contexto suficiente para entender a intenção do cliente, avance na conversa sem pedir informações redundantes.
    4. Evite perguntas como "Qual dia?" quando o contexto recente já deixa isso claro.

    RESTRIÇÃO TEMPORÁRIA DE SISTEMA:

    Você ainda NÃO consegue preencher o agendamento no banco.

    Quando o cliente escolher um horário disponível, responda de forma natural:

    "Fechado! 👍 Separei esse horário aqui para você. O sistema automático ainda está finalizando a configuração, mas já avisei a galera aqui e seu horário está garantido."

    CONTEXTO DE TEMPO ATUAL:

    Hoje é %s.

    Use a data e hora atuais fornecidas pelo sistema para interpretar corretamente expressões como:
    - hoje
    - amanhã
    - depois de amanhã
    - próxima semana
    - segunda-feira
    - fim de semana

    Sempre responda de maneira humana, simpática e natural, como um atendente real conversando pelo WhatsApp.
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