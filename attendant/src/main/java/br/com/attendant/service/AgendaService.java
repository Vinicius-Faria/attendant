package br.com.attendant.service;

import br.com.attendant.entity.Agenda;
import br.com.attendant.entity.Enterprise;

import java.time.LocalDate;
import java.util.List;

public interface AgendaService extends BaseService<Agenda, Long>{
    List<Agenda> findByDateAndEnterprise(LocalDate date, Enterprise enterprise);
}
