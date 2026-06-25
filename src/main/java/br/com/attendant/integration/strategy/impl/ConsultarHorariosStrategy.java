package br.com.attendant.integration.strategy.impl;

import br.com.attendant.entity.TimeTablesEnterprise;
import br.com.attendant.integration.context.GeminiToolContext;
import br.com.attendant.integration.strategy.GeminiToolStrategy;
import br.com.attendant.service.TimeTablesEnterpriseService;
import com.google.genai.types.FunctionDeclaration;
import com.google.genai.types.Schema;
import com.google.genai.types.Tool;
import com.google.genai.types.Type;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Component
class ConsultarHorariosStrategy implements GeminiToolStrategy {

    private static final String FUNCTION_NAME = "consultar_horarios_disponiveis";
    private static final String DATA_ARGUMENT = "data";
    private static final int DEFAULT_SLOT_INTERVAL_MINUTES = 30;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final TimeTablesEnterpriseService timeTablesEnterpriseService;

    ConsultarHorariosStrategy(TimeTablesEnterpriseService timeTablesEnterpriseService) {
        this.timeTablesEnterpriseService = timeTablesEnterpriseService;
    }

    @Override
    public String getFunctionName() {
        return FUNCTION_NAME;
    }

    @Override
    public Tool getToolDefinition() {
        Schema dataParam = Schema.builder()
                .type(Type.Known.STRING)
                .description("A data desejada no formato AAAA-MM-DD")
                .build();

        Schema propriedades = Schema.builder()
                .type(Type.Known.OBJECT)
                .properties(Map.of(DATA_ARGUMENT, dataParam))
                .required(List.of(DATA_ARGUMENT))
                .build();

        FunctionDeclaration declaration = FunctionDeclaration.builder()
                .name(FUNCTION_NAME)
                .description("Busca no sistema os horários disponíveis para uma determinada data.")
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

        LocalDate dataDesejada = parseDate(args);
        if (dataDesejada == null) {
            return "{\"status\": \"ERRO\", \"motivo\": \"Formato de data invalido. Forneça AAAA-MM-DD\"}";
        }

        List<TimeTablesEnterprise> agendaDoDia = timeTablesEnterpriseService.findByEnterpriseIdAndDayOfWeek(
                context.enterpriseId(),
                dataDesejada.getDayOfWeek()
        );

        List<String> horariosDisponiveis = montarHorariosDisponiveis(agendaDoDia);

        boolean estaFechadoNoDia = agendaDoDia.isEmpty() ||
                agendaDoDia.stream().allMatch(agenda -> Boolean.TRUE.equals(agenda.getIsClosed()));

        if (estaFechadoNoDia) {
            return "{\"status\": \"FECHADO\", \"data\": \"" + dataDesejada + "\", \"motivo\": \"Estabelecimento nao abre neste dia da semana.\"}";
        }

        if (horariosDisponiveis.isEmpty()) {
            return "{\"status\": \"SEM_VAGAS\", \"data\": \"" + dataDesejada + "\", \"motivo\": \"Todos os horarios estao preenchidos.\"}";
        }

        return "{\"status\": \"SUCESSO\", \"data\": \"" + dataDesejada + "\", \"horarios\": ["
                + String.join(", ", horariosDisponiveis.stream().map(h -> "\"" + h + "\"").toList()) + "]}";
    }

    private LocalDate parseDate(Map<String, Object> args) {
        Object dataArgument = args.get(DATA_ARGUMENT);
        if (dataArgument == null) {
            return null;
        }

        try {
            return LocalDate.parse(dataArgument.toString());
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private List<String> montarHorariosDisponiveis(List<TimeTablesEnterprise> agendaDoDia) {
        List<String> horariosDisponiveis = new ArrayList<>();

        agendaDoDia.stream()
                .filter(agenda -> !Boolean.TRUE.equals(agenda.getIsClosed()))
                .sorted(Comparator.comparing(TimeTablesEnterprise::getStartTime))
                .forEach(agenda -> horariosDisponiveis.addAll(montarSlots(agenda.getStartTime(), agenda.getEndTime())));

        return horariosDisponiveis;
    }

    private List<String> montarSlots(LocalTime startTime, LocalTime endTime) {
        List<String> slots = new ArrayList<>();
        LocalTime currentTime = startTime;

        while (currentTime.isBefore(endTime)) {
            slots.add(currentTime.format(TIME_FORMATTER));
            currentTime = currentTime.plusMinutes(DEFAULT_SLOT_INTERVAL_MINUTES);
        }

        return slots;
    }
}
