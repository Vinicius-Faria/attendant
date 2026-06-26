package br.com.attendant.service;

import br.com.attendant.entity.TimeTablesEnterprise;

import java.time.DayOfWeek;
import java.util.List;

public interface TimeTablesEnterpriseService extends BaseService<TimeTablesEnterprise, Long> {
    List<TimeTablesEnterprise> findByEnterpriseIdAndDayOfWeek(Long enterpriseId, DayOfWeek dayOfWeek);
}
