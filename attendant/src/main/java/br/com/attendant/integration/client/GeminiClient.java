package br.com.attendant.integration.client;

import br.com.attendant.entity.ChatMessage;
import br.com.attendant.entity.ChatSession;

import java.util.List;

public interface GeminiClient {
    String messageByWhatsApp(ChatSession session, List<ChatMessage> historicoPlanificado);
    String generateText(String prompt);
}
