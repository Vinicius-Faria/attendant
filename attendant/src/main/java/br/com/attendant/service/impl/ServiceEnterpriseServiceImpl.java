package br.com.attendant.service.impl;

import br.com.attendant.config.BusinessException;
import br.com.attendant.config.ExceptionEnum;
import br.com.attendant.entity.Enterprise;
import br.com.attendant.entity.ServiceEnterprise;
import br.com.attendant.repository.ServiceEnterpriseRepository;
import br.com.attendant.service.ServiceEnterpriseService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
class ServiceEnterpriseServiceImpl extends BaseServiceImpl<ServiceEnterprise, Long, ServiceEnterpriseRepository> implements ServiceEnterpriseService {

    ServiceEnterpriseServiceImpl(ServiceEnterpriseRepository serviceEnterpriseRepository) {
        super(serviceEnterpriseRepository);
    }

    public List<ServiceEnterprise> findByEnterprise(Enterprise enterprise){
        return repository.findByEnterprise(enterprise);
    }

    @Override
    public ServiceEnterprise findByIdAndEnterprise(Long id, Enterprise enterprise) {
        return repository.findByIdAndEnterprise(id, enterprise);
    }

    @Override
    public void validate(ServiceEnterprise entity) {
        if (entity.getEnterprise() == null || entity.getEnterprise().getId() == null) {
            throw new BusinessException(ExceptionEnum.ENTITY_INCOMPLETE, "É necessário informar o campo da Empresa associada ao Serviço.");
        }

        if (entity.getDuration() == null) {
            throw new BusinessException(ExceptionEnum.ENTITY_INCOMPLETE, "É necessário informar o campo de Duração do serviço.");
        }

        if (entity.getPrice() == null) {
            throw new BusinessException(ExceptionEnum.ENTITY_INCOMPLETE, "É necessário informar o campo de Valor do serviço.");
        }

        if (entity.getDescricao() == null) {
            throw new BusinessException(ExceptionEnum.ENTITY_INCOMPLETE, "É necessário informar o campo de Descrição do serviço.");
        }
    }
}
