package br.com.attendant.service;

import org.springframework.security.core.userdetails.UserDetails;

public interface EnterpriseDetailsService {
    UserDetails loadUserByUsername(String email);
}
