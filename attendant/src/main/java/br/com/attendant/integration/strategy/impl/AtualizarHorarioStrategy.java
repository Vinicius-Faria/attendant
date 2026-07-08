package br.com.attendant.integration.strategy.impl;

import br.com.attendant.entity.Agenda;
import br.com.attendant.integration.context.GeminiToolContext;
import br.com.attendant.integration.strategy.GeminiToolStrategy;
import br.com.attendant.service.AgendaService;
import br.com.attendant.service.TimeTablesEnterpriseService;
import com.google.genai.types.FunctionDeclaration;
import com.google.genai.types.Schema;
import com.google.genai.types.Tool;
import com.google.genai.types.Type;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
class AtualizarHorarioStrategy implements GeminiToolStrategy {

    private static final String FUNCTION_NAME = "atualizar_horario_agendado";
    private static final String ARG_NOVA_DATA_HORA = "novaDataHoraISO";

    private final AgendaService agendaService;
    private final TimeTablesEnterpriseService timeTablesEnterpriseService;

    public AtualizarHorarioStrategy(AgendaService agendaService,
                                    TimeTablesEnterpriseService timeTablesEnterpriseService) {
        this.agendaService = agendaService;
        this.timeTablesEnterpriseService = timeTablesEnterpriseService;
    }

    @Override
    public String getFunctionName() {
        return FUNCTION_NAME;
    }

    @Override
    public Tool getToolDefinition() {
        Schema novaDataHoraParam = Schema.builder()
                .type(Type.Known.STRING)
                .description("Nova data e hora desejada pelo cliente no formato ISO-8601 (AAAA-MM-DDTHH:MM:SS).")
                .build();

        Schema propriedades = Schema.builder()
                .type(Type.Known.OBJECT)
                .properties(Map.of(ARG_NOVA_DATA_HORA, novaDataHoraParam))
                .required(List.of(ARG_NOVA_DATA_HORA))
                .build();

        FunctionDeclaration declaration = FunctionDeclaration.builder()
                .name(FUNCTION_NAME)
                .description("Altera a data e o horário de um agendamento ativo existente para este cliente.")
                .parameters(propriedades)
                .build();

        return Tool.builder()
                .functionDeclarations(List.of(declaration))
                .build();
    }

    @Override
    public String execute(Map<String, Object> args, GeminiToolContext context) {
        if (context.enterpriseId() == null) {
            return "{\"status\": \"ERRO\", \"motivo\": \"EnterpriseId nao fornecido no contexto\"}";
        }

        if (context.chatSession() == null) {
            return "{\"status\": \"ERRO\", \"motivo\": \"ChatSession nao fornecida no contexto\"}";
        }

        try {
            LocalDateTime novaDataHora = LocalDateTime.parse(args.get(ARG_NOVA_DATA_HORA).toString());
            Agenda agendaAtiva = agendaService.findByChatSession(context.chatSession());

            if (agendaAtiva == null || !Boolean.TRUE.equals(agendaAtiva.getIsActive())) {
                return "{\"status\": \"ERRO\", \"motivo\": \"Nenhum agendamento ativo encontrado para ser atualizado.\"}";
            }

            agendaAtiva.setAtDateHour(novaDataHora);
            agendaAtiva.setTimeTablesEnterprise(timeTablesEnterpriseService.findByEnterpriseIdAndDayOfWeek(
                    context.enterpriseId(),
                    novaDataHora.getDayOfWeek()
            ));

            Agenda agendaAtualizada = agendaService.update(agendaAtiva, agendaAtiva.getId());
            return "{\"status\": \"SUCESSO\", \"agendamentoId\": " + agendaAtualizada.getId() + "}";

        } catch (Exception e) {
            e.printStackTrace();
            return "{\"status\": \"ERRO\", \"motivo\": \"Falha ao atualizar o agendamento no banco: " + e.getMessage() + "\"}";
        }
    }
}