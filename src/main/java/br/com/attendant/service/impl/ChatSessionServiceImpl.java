package br.com.attendant.service.impl;

import br.com.attendant.config.BusinessException;
import br.com.attendant.config.ExceptionEnum;
import br.com.attendant.entity.ChatSession;
import br.com.attendant.repository.ChatSessionRepository;
import br.com.attendant.service.ChatSessionService;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
class ChatSessionServiceImpl extends BaseServiceImpl<ChatSession, Long, ChatSessionRepository> implements ChatSessionService {

    ChatSessionServiceImpl(ChatSessionRepository chatSessionRepository) {
        super(chatSessionRepository);
    }

    @Override
    public List<ChatSession> findByHistoricByNumberClient(String numberClient) {
        List<ChatSession> chatSessionList = repository.findByNumberPhoneClient(numberClient);

        return chatSessionList.stream()
                .sorted(Comparator.comparing(ChatSession::getUpdatedAt))
                .toList();
    }

    @Override
    public void validate(ChatSession entity) {
        if (entity.getEnterprise() == null || entity.getEnterprise().getId() == null) {
            throw new BusinessException(ExceptionEnum.ENTITY_INCOMPLETE, "É necessário informar o campo da empresa.");
        }

        if (entity.getNumberPhoneClient() == null || entity.getNumberPhoneClient().isEmpty()) {
            throw new BusinessException(ExceptionEnum.ENTITY_INCOMPLETE, "É necessário informar o campo do número do usuário.");
        }

        if (entity.getCreatedAt() == null) {
            throw new BusinessException(ExceptionEnum.ENTITY_INCOMPLETE, "É necessário informar o campo da data/hora de criação.");
        }
    }
}
