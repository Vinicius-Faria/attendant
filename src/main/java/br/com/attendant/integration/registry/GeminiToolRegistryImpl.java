package br.com.attendant.integration.registry;

import br.com.attendant.integration.GeminiToolRegistry;
import br.com.attendant.integration.strategy.GeminiToolStrategy;
import com.google.genai.types.Tool;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
class GeminiToolRegistryImpl implements GeminiToolRegistry {

    private final Map<String, GeminiToolStrategy> strategies = new ConcurrentHashMap<>();

    GeminiToolRegistryImpl(List<GeminiToolStrategy> strategies) {
        strategies.forEach(this::register);
    }

    @Override
    public GeminiToolStrategy getStrategy(String functionName) {
        return strategies.get(functionName);
    }

    @Override
    public List<Tool> getAllTools() {
        return strategies.values().stream()
                .map(GeminiToolStrategy::getToolDefinition)
                .toList();
    }

    private void register(GeminiToolStrategy strategy) {
        strategies.put(strategy.getFunctionName(), strategy);
    }
}
