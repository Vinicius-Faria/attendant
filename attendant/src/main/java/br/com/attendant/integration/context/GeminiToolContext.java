package br.com.attendant.integration.context;

import br.com.attendant.entity.ChatSession;

public record GeminiToolContext(Long enterpriseId, ChatSession chatSession) {
}
