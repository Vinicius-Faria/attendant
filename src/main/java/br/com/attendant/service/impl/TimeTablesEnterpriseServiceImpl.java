package br.com.attendant.service.impl;

import br.com.attendant.config.BusinessException;
import br.com.attendant.config.ExceptionEnum;
import br.com.attendant.entity.TimeTablesEnterprise;
import br.com.attendant.repository.TimeTablesEnterpriseRepository;
import br.com.attendant.service.TimeTablesEnterpriseService;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.util.List;

@Service
class TimeTablesEnterpriseServiceImpl extends BaseServiceImpl<TimeTablesEnterprise, Long, TimeTablesEnterpriseRepository> implements TimeTablesEnterpriseService {

    TimeTablesEnterpriseServiceImpl(TimeTablesEnterpriseRepository timeTablesEnterpriseRepository) {
        super(timeTablesEnterpriseRepository);
    }

    @Override
    public List<TimeTablesEnterprise> findByEnterpriseIdAndDayOfWeek(Long enterpriseId, DayOfWeek dayOfWeek) {
        return repository.findByEnterprise_IdAndDayOfWeek(enterpriseId, dayOfWeek);
    }

    @Override
    public void validate(TimeTablesEnterprise entity) {
        if (entity.getEnterprise() == null || entity.getEnterprise().getId() == null) {
            throw new BusinessException(ExceptionEnum.ENTITY_INCOMPLETE, "É necessário informar o campo da Empresa associada às datas e horários.");
        }

        if (entity.getEndTime() == null) {
            throw new BusinessException(ExceptionEnum.ENTITY_INCOMPLETE, "É necessário informar o campo de qual hora irá fechar.");
        }

        if (entity.getStartTime() == null) {
            throw new BusinessException(ExceptionEnum.ENTITY_INCOMPLETE, "É necessário informar o campo de qual hora irá abrir.");
        }

        if (entity.getDayOfWeek() == null) {
            throw new BusinessException(ExceptionEnum.ENTITY_INCOMPLETE, "É necessário informar o campo do dia.");
        }
    }
}
