package br.com.attendant.service.impl;

import br.com.attendant.entity.Enterprise;
import br.com.attendant.entity.EnterpriseDetails;
import br.com.attendant.service.EnterpriseDetailsService;
import br.com.attendant.service.EnterpriseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
class CustomEnterpriseDetailServiceImpl implements UserDetailsService, EnterpriseDetailsService {

    @Autowired
    private EnterpriseService enterpriseService;

    @Override
    public UserDetails loadUserByUsername(String email) {
        Enterprise enterprise = enterpriseService.findEnterpriseByEmail(email);
        return new EnterpriseDetails(enterprise);
    }
}
