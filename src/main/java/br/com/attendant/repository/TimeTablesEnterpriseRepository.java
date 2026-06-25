package br.com.attendant.repository;

import br.com.attendant.entity.TimeTablesEnterprise;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.util.List;

public interface TimeTablesEnterpriseRepository extends JpaRepository<TimeTablesEnterprise, Long> {
    List<TimeTablesEnterprise> findByEnterprise_IdAndDayOfWeek(Long enterpriseId, DayOfWeek dayOfWeek);
}
