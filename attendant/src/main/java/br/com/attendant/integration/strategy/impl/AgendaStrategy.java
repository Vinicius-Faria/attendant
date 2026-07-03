package br.com.attendant.integration.strategy.impl;

import br.com.attendant.entity.*;
import br.com.attendant.integration.context.GeminiToolContext;
import br.com.attendant.integration.strategy.GeminiToolStrategy;
import br.com.attendant.service.*;
import com.google.genai.types.FunctionDeclaration;
import com.google.genai.types.Schema;
import com.google.genai.types.Tool;
import com.google.genai.types.Type;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class AgendaStrategy implements GeminiToolStrategy {

    private static final String FUNCTION_NAME = "realizar_agendamento";
    private static final String ARG_NOME_CLIENTE = "nomeCliente";
    private static final String ARG_SERVICE_ID = "serviceId";
    private static final String ARG_DATA_HORA = "dataHoraISO";

    private final AgendaService agendaService;
    private final ServiceEnterpriseService serviceEnterpriseService;
    private final TimeTablesEnterpriseService timeTablesEnterpriseService;
    private final ChatSessionService chatSessionService;

    public AgendaStrategy(AgendaService agendaService,
                          ServiceEnterpriseService serviceEnterpriseService,
                          TimeTablesEnterpriseService timeTablesEnterpriseService,
                          ChatSessionService chatSessionService) {
        this.agendaService = agendaService;
        this.serviceEnterpriseService = serviceEnterpriseService;
        this.timeTablesEnterpriseService = timeTablesEnterpriseService;
        this.chatSessionService = chatSessionService;
    }

    @Override
    public String getFunctionName() {
        return FUNCTION_NAME;
    }

    @Override
    public Tool getToolDefinition() {
        Schema nomeParam = Schema.builder()
                .type(Type.Known.STRING)
                .description("Nome do cliente retornado na conversa.")
                .build();

        Schema serviceIdParam = Schema.builder()
                .type(Type.Known.INTEGER)
                .description("O ID numérico exato obtido na consulta do serviço.")
                .build();

        Schema dataHoraParam = Schema.builder()
                .type(Type.Known.STRING)
                .description("Data e hora no formato ISO-8601 (AAAA-MM-DDTHH:MM:SS).")
                .build();

        Schema propriedades = Schema.builder()
                .type(Type.Known.OBJECT)
                .properties(Map.of(
                        ARG_NOME_CLIENTE, nomeParam,
                        ARG_SERVICE_ID, serviceIdParam,
                        ARG_DATA_HORA, dataHoraParam
                ))
                .required(List.of(ARG_NOME_CLIENTE, ARG_SERVICE_ID, ARG_DATA_HORA))
                .build();

        FunctionDeclaration declaration = FunctionDeclaration.builder()
                .name(FUNCTION_NAME)
                .description("Efetiva e grava o agendamento no banco de dados.")
                .parameters(propriedades)
                .build();

        return Tool.builder()
                .functionDeclarations(List.of(declaration))
                .build();
    }

    @Override
    public String execute(Map<String, Object> args, GeminiToolContext context) {
        if (context.enterpriseId() == null) {
            return "{\"status\": \"ERRO\", \"motivo\": \"EnterpriseId não fornecido.\"}";
        }

        try {
            String nomeCliente = args.get(ARG_NOME_CLIENTE).toString();

            String rawServiceId = args.get(ARG_SERVICE_ID).toString().replaceAll("\\.0$", "");
            Long serviceId = Long.valueOf(rawServiceId);

            LocalDateTime dataHora = LocalDateTime.parse(args.get(ARG_DATA_HORA).toString());

            ServiceEnterprise servico = serviceEnterpriseService.findByIdAndEnterprise(serviceId, new Enterprise(context.enterpriseId()));

            if (servico == null) {
                System.err.println("[AGENDA ERRO] O Gemini tentou agendar usando o serviceId: " + serviceId + " mas ele não existe para a Enterprise: " + context.enterpriseId());
                return "{\"status\": \"ERRO\", \"motivo\": \"ID de serviço " + serviceId + " inválido para esta empresa.\"}";
            }

            Agenda agenda = new Agenda();
            agenda.setEnterprise(new Enterprise(context.enterpriseId()));
            agenda.setServiceEnterprise(servico);
            agenda.setScheduledFrom(nomeCliente);
            agenda.setAtDateHour(dataHora);
            agenda.setIsActive(true);

            if (context.chatSession() != null) {
                agenda.setChatSession(context.chatSession());
            }

            agenda.setTimeTablesEnterprise(timeTablesEnterpriseService.findByEnterpriseIdAndDayOfWeek(
                    context.enterpriseId(),
                    dataHora.getDayOfWeek()
            ));
            Agenda agendamentoSalvo = agendaService.save(agenda);

            return "{\"status\": \"SUCESSO\", \"agendamentoId\": " + agendamentoSalvo.getId() + "}";

        } catch (Exception e) {
            e.printStackTrace();
            return "{\"status\": \"ERRO\", \"motivo\": \"Falha ao salvar no banco: " + e.getMessage() + "\"}";
        }
    }
}