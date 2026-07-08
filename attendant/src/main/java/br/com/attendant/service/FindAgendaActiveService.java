package br.com.attendant.service;

import br.com.attendant.dto.ContextDto;

public interface FindAgendaActiveService {
    ContextDto  findActiveContext(String message, String numberReceived, String numberSent);
}
