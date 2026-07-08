package br.com.attendant.integration.strategy.impl;

import br.com.attendant.entity.Agenda;
import br.com.attendant.integration.context.GeminiToolContext;
import br.com.attendant.integration.strategy.GeminiToolStrategy;
import br.com.attendant.service.AgendaService;
import com.google.genai.types.FunctionDeclaration;
import com.google.genai.types.Schema;
import com.google.genai.types.Tool;
import com.google.genai.types.Type;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
class CancelamentoHorarioStrategy implements GeminiToolStrategy {

    private static final String FUNCTION_NAME = "cancelar_servico_agendado";

    private final AgendaService agendaService;

    public CancelamentoHorarioStrategy(AgendaService agendaService) {
        this.agendaService = agendaService;
    }

    @Override
    public String getFunctionName() {
        return FUNCTION_NAME;
    }

    @Override
    public Tool getToolDefinition() {
        Schema propriedades = Schema.builder()
                .type(Type.Known.OBJECT)
                .properties(Map.of())
                .build();

        FunctionDeclaration declaration = FunctionDeclaration.builder()
                .name(FUNCTION_NAME)
                .description("Busca o agendamento ativo vinculado à conversa atual do cliente e realiza o cancelamento no sistema.")
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
            Agenda agendaAtiva = agendaService.findByChatSession(context.chatSession());
            if (agendaAtiva == null || !Boolean.TRUE.equals(agendaAtiva.getIsActive())) {
                return "{\"status\": \"ERRO\", \"motivo\": \"Nenhum agendamento ativo foi encontrado para esta conversa.\"}";
            }

            agendaAtiva.setIsActive(false);
            agendaService.update(agendaAtiva, agendaAtiva.getId());

            return "{\"status\": \"SUCESSO\", \"motivo\": \"Agendamento cancelado com sucesso no sistema.\"}";

        } catch (Exception e) {
            e.printStackTrace();
            return "{\"status\": \"ERRO\", \"motivo\": \"Falha ao cancelar o agendamento no banco: " + e.getMessage() + "\"}";
        }
    }
}