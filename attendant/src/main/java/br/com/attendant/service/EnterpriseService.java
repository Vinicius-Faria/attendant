package br.com.attendant.service;

import br.com.attendant.entity.Enterprise;

public interface EnterpriseService extends BaseService<Enterprise, Long> {
    Enterprise findEnterpriseByCnpj(String cnpj);
    Enterprise findEnterpriseByEmail(String email);
}
