package br.com.attendant.service.impl;

import br.com.attendant.entity.ChatMessage;
import br.com.attendant.entity.ChatSession;
import br.com.attendant.entity.MessageRole;
import br.com.attendant.entity.SessionStatus;
import br.com.attendant.entity.WhatsAppConfig;
import br.com.attendant.integration.client.GeminiClient;
import br.com.attendant.service.ChatMessageService;
import br.com.attendant.service.ChatSessionService;
import br.com.attendant.service.WhatsAppConfigService;
import br.com.attendant.service.WhatsAppMessageService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
class WhatsAppMessageServiceImpl implements WhatsAppMessageService {

    private final WhatsAppConfigService whatsAppConfigService;
    private final ChatSessionService chatSessionService;
    private final ChatMessageService chatMessageService;
    private final GeminiClient geminiClient;

    WhatsAppMessageServiceImpl(
            WhatsAppConfigService whatsAppConfigService,
            ChatSessionService chatSessionService,
            ChatMessageService chatMessageService,
            GeminiClient geminiClient
    ) {
        this.whatsAppConfigService = whatsAppConfigService;
        this.chatSessionService = chatSessionService;
        this.chatMessageService = chatMessageService;
        this.geminiClient = geminiClient;
    }

    @Override
    public String receiveMessage(String message, String numberReceived, String numberSent) {
        WhatsAppConfig whatsAppConfig = whatsAppConfigService.findByNumberOfEnterprise(numberReceived);
        ChatSession chatSession = loadOrCreateSession(whatsAppConfig, numberSent);

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setCreatedAt(LocalDateTime.now());
        chatMessage.setRole(MessageRole.USER);
        chatMessage.setContent(message);
        chatMessage.setSession(chatSession);

        persistSession(chatSession);
        chatMessageService.save(chatMessage);

        List<ChatMessage> chatMessageList = chatMessageService.findByChatSession(chatSession);
        return geminiClient.messageByWhatsApp(chatSession, chatMessageList);
    }

    @Override
    public List<ChatMessage> findHistoric(String numberSent) {
        List<ChatSession> chatSessionList = chatSessionService.findByHistoricByNumberClient(numberSent);
        return chatMessageService.findByChatSession(chatSessionList.get(0));
    }

    private ChatSession loadOrCreateSession(WhatsAppConfig whatsAppConfig, String numberSent) {
        List<ChatSession> chatSessionList = chatSessionService.findByHistoricByNumberClient(numberSent);

        if (!chatSessionList.isEmpty() && chatSessionList.get(0).getStatus().equals(SessionStatus.EM_ANDAMENTO)) {
            ChatSession chatSession = chatSessionList.get(0);
            chatSession.setUpdatedAt(LocalDateTime.now());
            return chatSession;
        }

        ChatSession chatSession = new ChatSession();
        chatSession.setEnterprise(whatsAppConfig.getEnterprise());
        chatSession.setStatus(SessionStatus.EM_ANDAMENTO);
        chatSession.setCreatedAt(LocalDateTime.now());
        chatSession.setNumberPhoneClient(numberSent);
        chatSession.setUpdatedAt(LocalDateTime.now());
        return chatSession;
    }

    private void persistSession(ChatSession chatSession) {
        if (chatSession.getId() != null) {
            try {
                chatSessionService.update(chatSession, chatSession.getId());
                return;
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }

        chatSessionService.save(chatSession);
    }
}
