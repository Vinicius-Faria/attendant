package br.com.attendant.service.impl;

import br.com.attendant.config.BusinessException;
import br.com.attendant.config.ExceptionEnum;
import br.com.attendant.entity.ChatMessage;
import br.com.attendant.entity.ChatSession;
import br.com.attendant.repository.ChatMessageRepository;
import br.com.attendant.service.ChatMessageService;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
class ChatMessageServiceImpl extends BaseServiceImpl<ChatMessage, Long, ChatMessageRepository> implements ChatMessageService {

    ChatMessageServiceImpl(ChatMessageRepository chatMessageRepository) {
        super(chatMessageRepository);
    }

    @Override
    public List<ChatMessage> findByChatSession(ChatSession chatSession) {
        List<ChatMessage> chatMessageList = repository.findBySession(chatSession);

        return chatMessageList.stream()
                .sorted(Comparator.comparing(ChatMessage::getCreatedAt))
                .toList();
    }

    @Override
    public void validate(ChatMessage entity) {
        if (entity.getContent() == null) {
            throw new BusinessException(ExceptionEnum.ENTITY_INCOMPLETE, "É necessário informar a mensagem do chat.");
        }

        if (entity.getSession() == null || entity.getSession().getId() == null) {
            throw new BusinessException(ExceptionEnum.ENTITY_INCOMPLETE, "É necessário informar o Chat da sessão.");
        }

        if (entity.getCreatedAt() == null) {
            throw new BusinessException(ExceptionEnum.ENTITY_INCOMPLETE, "É necessário informar a data/hora da criação da mensagem.");
        }
    }
}
