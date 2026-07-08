package br.com.attendant.service.impl;

import br.com.attendant.dto.ContextDto;
import br.com.attendant.entity.ChatMessage;
import br.com.attendant.entity.ChatSession;
import br.com.attendant.entity.MessageRole;
import br.com.attendant.entity.SessionStatus;
import br.com.attendant.entity.WhatsAppConfig;
import br.com.attendant.integration.client.GeminiClient;
import br.com.attendant.service.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
class WhatsAppMessageServiceImpl implements WhatsAppMessageService {

    private final ChatSessionService chatSessionService;
    private final ChatMessageService chatMessageService;
    private final GeminiClient geminiClient;
    private final FindAgendaActiveService findAgendaActiveService;

    WhatsAppMessageServiceImpl(
            ChatSessionService chatSessionService,
            ChatMessageService chatMessageService,
            GeminiClient geminiClient,
            FindAgendaActiveService findAgendaActiveService
    ) {
        this.chatSessionService = chatSessionService;
        this.chatMessageService = chatMessageService;
        this.geminiClient = geminiClient;
        this.findAgendaActiveService = findAgendaActiveService;
    }

    @Override
    public String receiveMessage(String message, String numberReceived, String numberSent) {
        ContextDto context = findAgendaActiveService.findActiveContext(message, numberReceived, numberSent);
        return geminiClient.messageByWhatsApp(context);
    }

    @Override
    public List<ChatMessage> findHistoric(String numberSent) {
        List<ChatSession> chatSessionList = chatSessionService.findByHistoricByNumberClient(numberSent);
        return chatMessageService.findByChatSession(chatSessionList.get(0));
    }
}
