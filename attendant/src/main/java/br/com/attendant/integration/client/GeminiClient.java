package br.com.attendant.integration.client;

import br.com.attendant.dto.ContextDto;
import br.com.attendant.entity.ChatMessage;
import br.com.attendant.entity.ChatSession;

import java.util.List;

public interface GeminiClient {
    String messageByWhatsApp(ContextDto context);
    String generateText(String prompt);
}
