package br.com.attendant.entity;

import br.com.attendant.service.EnterpriseDetailsService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class EnterpriseDetails implements UserDetails {
    private final Enterprise enterprise;

    public EnterpriseDetails(Enterprise enterprise) {
        this.enterprise = enterprise;
    }

    public UUID getUuid(){
        return enterprise.getUuid();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return enterprise.getSenha();
    }

    @Override
    public String getUsername() {
        return enterprise.getEmail();
    }

    @Override
    public boolean isEnabled() {
        return Boolean.TRUE.equals(enterprise.getEmailValido());
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
}
