package br.com.attendant.service;

import br.com.attendant.entity.Agenda;
import br.com.attendant.entity.ChatSession;
import br.com.attendant.entity.Enterprise;

import java.time.LocalDate;
import java.util.List;

public interface AgendaService extends BaseService<Agenda, Long>{
    Agenda findByChatSession(ChatSession chatSession);
    List<Agenda> findByDateAndEnterprise(LocalDate date, Enterprise enterprise);
}
