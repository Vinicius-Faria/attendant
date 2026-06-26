package br.com.attendant.service;

import br.com.attendant.entity.ChatMessage;

import java.util.List;

public interface WhatsAppMessageService {
    String receiveMessage(String message, String numberReceived, String numberSent);

    List<ChatMessage> findHistoric(String numberSent);
}
