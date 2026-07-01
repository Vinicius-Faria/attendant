package br.com.attendant.repository;

import br.com.attendant.entity.Agenda;
import br.com.attendant.entity.Enterprise;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface AgendaRepository extends JpaRepository<Agenda, Long> {
    List<Agenda> findByAtDateHourBetweenAndEnterprise(LocalDateTime start, LocalDateTime end, Enterprise enterprise);
}
