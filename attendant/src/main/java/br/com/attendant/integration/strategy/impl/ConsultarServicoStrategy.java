package br.com.attendant.integration.strategy.impl;

import br.com.attendant.entity.Enterprise;
import br.com.attendant.entity.ServiceEnterprise;
import br.com.attendant.integration.context.GeminiToolContext;
import br.com.attendant.integration.strategy.GeminiToolStrategy;
import br.com.attendant.service.ServiceEnterpriseService;
import com.google.genai.types.FunctionDeclaration;
import com.google.genai.types.Schema;
import com.google.genai.types.Tool;
import com.google.genai.types.Type;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
class ConsultarServicoStrategy implements GeminiToolStrategy {

    private static final String FUNCTION_NAME = "consultar_servicos_disponiveis";
    private final ServiceEnterpriseService serviceEnterpriseService;

    ConsultarServicoStrategy(ServiceEnterpriseService serviceEnterpriseService) {
        this.serviceEnterpriseService = serviceEnterpriseService;
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
                .description("Busca no sistema o catálogo de serviços prestados pela empresa, incluindo descrição, preço e duração.")
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

        List<ServiceEnterprise> servicos = serviceEnterpriseService.findByEnterprise(new Enterprise(context.enterpriseId()));

        if (servicos == null || servicos.isEmpty()) {
            return "{\"status\": \"SEM_SERVICOS\", \"motivo\": \"Nenhum servico cadastrado para esta empresa.\"}";
        }

        return montarRetornoSucesso(servicos);
    }

    private String montarRetornoSucesso(List<ServiceEnterprise> servicos) {
        List<String> jsonServicos = servicos.stream().map(s -> String.format(
                "{\"id\": %d, \"descricao\": \"%s\", \"preco\": %.2f, \"duracao_minutos\": %d}",
                s.getId(),
                escapeJson(s.getDescricao()),
                s.getPrice() != null ? s.getPrice() : 0.0,
                s.getDuration() != null ? s.getDuration() : 0
        )).toList();

        return "{\"status\": \"SUCESSO\", \"servicos\": [" + String.join(", ", jsonServicos) + "]}";
    }

    private String escapeJson(String valor) {
        if (valor == null) return "";
        return valor.replace("\"", "\\\"");
    }
}