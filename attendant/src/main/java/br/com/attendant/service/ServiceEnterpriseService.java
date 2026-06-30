package br.com.attendant.service;

import br.com.attendant.entity.Enterprise;
import br.com.attendant.entity.ServiceEnterprise;

import java.util.List;

public interface ServiceEnterpriseService extends BaseService<ServiceEnterprise, Long> {

    List<ServiceEnterprise> findByEnterprise(Enterprise enterprise);

}
