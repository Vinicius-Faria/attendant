package br.com.attendant.service;

import br.com.attendant.entity.ChatSession;

import java.util.List;

public interface ChatSessionService extends BaseService<ChatSession, Long> {
    List<ChatSession> findByHistoricByNumberClient(String numberClient);
}
