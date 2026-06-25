package br.com.attendant.service.impl;

import br.com.attendant.config.BusinessException;
import br.com.attendant.config.ExceptionEnum;
import br.com.attendant.entity.WhatsAppConfig;
import br.com.attendant.repository.WhatsAppConfigRepository;
import br.com.attendant.service.WhatsAppConfigService;
import org.springframework.stereotype.Service;

@Service
class WhatsAppConfigServiceImpl extends BaseServiceImpl<WhatsAppConfig, Long, WhatsAppConfigRepository> implements WhatsAppConfigService {

    WhatsAppConfigServiceImpl(WhatsAppConfigRepository whatsAppConfigRepository) {
        super(whatsAppConfigRepository);
    }

    @Override
    public WhatsAppConfig findByNumberOfEnterprise(String numberEnterprise) {
        return repository.findByNumberPhone(numberEnterprise);
    }

    @Override
    public void validate(WhatsAppConfig entity) {
        if (entity.getNumberPhone() == null || entity.getNumberPhone().isEmpty()) {
            throw new BusinessException(ExceptionEnum.ENTITY_INCOMPLETE, "É necessário informar o campo de Número de telefone.");
        }

        if (entity.getEnterprise() == null || entity.getEnterprise().getId() == null) {
            throw new BusinessException(ExceptionEnum.ENTITY_INCOMPLETE, "É necessário informar o campo da Empresa associada a esse Número de telefone");
        }

        if (entity.getApiToken() == null || entity.getApiToken().isEmpty()) {
            throw new BusinessException(ExceptionEnum.ENTITY_INCOMPLETE, "É necessário informar o campo do Token gerado no WhatsApp.");
        }
    }
}
