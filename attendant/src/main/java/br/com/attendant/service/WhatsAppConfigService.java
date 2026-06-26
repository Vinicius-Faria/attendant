package br.com.attendant.service;

import br.com.attendant.entity.WhatsAppConfig;

public interface WhatsAppConfigService extends BaseService<WhatsAppConfig, Long> {
    WhatsAppConfig findByNumberOfEnterprise(String numberEnterprise);
}
