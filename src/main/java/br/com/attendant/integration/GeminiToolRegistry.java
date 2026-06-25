package br.com.attendant.integration;

import br.com.attendant.integration.strategy.GeminiToolStrategy;
import com.google.genai.types.Tool;
import java.util.List;

public interface GeminiToolRegistry {
    GeminiToolStrategy getStrategy(String functionName);
    List<Tool> getAllTools();
}
