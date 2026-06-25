package br.com.attendant.service.impl;

import br.com.attendant.config.BusinessException;
import br.com.attendant.config.ExceptionEnum;
import br.com.attendant.entity.Enterprise;
import br.com.attendant.repository.EnterpriseRepository;
import br.com.attendant.service.EnterpriseService;
import org.springframework.stereotype.Service;

@Service
class EnterpriseServiceImpl extends BaseServiceImpl<Enterprise, Long, EnterpriseRepository> implements EnterpriseService {

    EnterpriseServiceImpl(EnterpriseRepository enterpriseRepository) {
        super(enterpriseRepository);
    }

    @Override
    public Enterprise findEnterpriseByCnpj(String cnpj) {
        return repository.findByCnpj(cnpj);
    }

    @Override
    public void validate(Enterprise entity) {
        if (entity.getCnpj() == null || entity.getCnpj().isEmpty()) {
            throw new BusinessException(ExceptionEnum.ENTITY_INCOMPLETE, "É necessário informar o campo de CNPJ.");
        }

        if (entity.getDescricao() == null || entity.getDescricao().isEmpty()) {
            throw new BusinessException(ExceptionEnum.ENTITY_INCOMPLETE, "É necessário informar o campo de descrição.");
        }
    }
}
