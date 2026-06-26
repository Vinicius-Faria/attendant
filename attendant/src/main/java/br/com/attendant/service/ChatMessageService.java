package br.com.attendant.service;

import br.com.attendant.entity.ChatMessage;
import br.com.attendant.entity.ChatSession;

import java.util.List;

public interface ChatMessageService extends BaseService<ChatMessage, Long> {
    List<ChatMessage> findByChatSession(ChatSession chatSession);
}
