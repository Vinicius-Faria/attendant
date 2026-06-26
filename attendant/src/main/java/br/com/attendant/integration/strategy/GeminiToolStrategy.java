package br.com.attendant.integration.strategy;

import br.com.attendant.integration.context.GeminiToolContext;
import com.google.genai.types.Tool;

import java.util.Map;

public interface GeminiToolStrategy {
    String getFunctionName();
    Tool getToolDefinition();
    String execute(Map<String, Object> args, GeminiToolContext context);
}
