package br.com.attendant.service.impl;

import br.com.attendant.dto.ContextDto;
import br.com.attendant.entity.Agenda;
import br.com.attendant.entity.ChatMessage;
import br.com.attendant.entity.ChatSession;
import br.com.attendant.entity.MessageRole;
import br.com.attendant.entity.SessionStatus;
import br.com.attendant.entity.WhatsAppConfig;
import br.com.attendant.service.AgendaService;
import br.com.attendant.service.ChatSessionService;
import br.com.attendant.service.ChatMessageService;
import br.com.attendant.service.FindAgendaActiveService;
import br.com.attendant.service.WhatsAppConfigService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
class FindAgendaActiveImpl implements FindAgendaActiveService {

    private final WhatsAppConfigService whatsAppConfigService;
    private final ChatSessionService chatSessionService;
    private final AgendaService agendaService;
    private final ChatMessageService chatMessageService;
    private final Integer NUMERO_MESSAGE_BY_AGENDADO = 4;

    FindAgendaActiveImpl(
            WhatsAppConfigService whatsAppConfigService,
            ChatSessionService chatSessionService,
            AgendaService agendaService,
            ChatMessageService chatMessageService) {
        this.whatsAppConfigService = whatsAppConfigService;
        this.chatSessionService = chatSessionService;
        this.agendaService = agendaService;
        this.chatMessageService = chatMessageService;
    }

    @Override
    @Transactional
    public ContextDto findActiveContext(String message, String numberReceived, String numberSent) {
        ContextDto contextDto = new ContextDto();

        WhatsAppConfig whatsAppConfig = whatsAppConfigService.findByNumberOfEnterprise(numberReceived);
        contextDto.setEnterprise(whatsAppConfig.getEnterprise());
        List<ChatSession> chatSessionList = chatSessionService.findByHistoricByNumberClient(numberSent);

        ChatSession currentCompanySession = chatSessionList.stream()
                .filter(session -> session.getEnterprise().getId().equals(whatsAppConfig.getEnterprise().getId()))
                .findFirst()
                .orElse(null);

        ChatSession chatSession;
        boolean estavaConcluido = false;

        if (currentCompanySession != null) {
            chatSession = currentCompanySession;
            chatSession.setUpdatedAt(LocalDateTime.now());

            if (chatSession.getStatus().equals(SessionStatus.EM_ANDAMENTO)) {
                contextDto.setAgenda(null);
            } else {
                Agenda agenda = agendaService.findByChatSession(chatSession);
                contextDto.setAgenda(agenda);
                chatSession.setStatus(SessionStatus.EM_ANDAMENTO);
                estavaConcluido = true;
            }
        } else {
            chatSession = createNewChatSession(whatsAppConfig, numberSent);
            contextDto.setAgenda(null);
        }

        persistSession(chatSession);
        contextDto.setChatSession(chatSession);

        ChatMessage newChatMessage = new ChatMessage();
        newChatMessage.setCreatedAt(LocalDateTime.now());
        newChatMessage.setRole(MessageRole.USER);
        newChatMessage.setContent(message);
        newChatMessage.setSession(chatSession);
        chatMessageService.save(newChatMessage);

        if (estavaConcluido) {
            List<ChatMessage> allMessages = chatMessageService.findByChatSession(chatSession);
            ChatMessage lastSaved = allMessages.get(allMessages.size() - 1);
            List<ChatMessage> oldMessages = allMessages.subList(0, allMessages.size() - 1);

            List<ChatMessage> limitedOldMessages = oldMessages.stream()
                    .skip(Math.max(0, oldMessages.size() - NUMERO_MESSAGE_BY_AGENDADO))
                    .toList();

            List<ChatMessage> optimizedList = new ArrayList<>(limitedOldMessages);
            optimizedList.add(lastSaved);

            contextDto.setChatMessageList(optimizedList);
        } else {
            List<ChatMessage> chatMessageList = chatMessageService.findByChatSession(chatSession);
            contextDto.setChatMessageList(chatMessageList);
        }

        return contextDto;
    }

    private ChatSession createNewChatSession(WhatsAppConfig whatsAppConfig, String numberSent) {
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