package br.com.attendant.repository;

import br.com.attendant.entity.WhatsAppConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WhatsAppConfigRepository extends JpaRepository<WhatsAppConfig, Long> {

    WhatsAppConfig findByNumberPhone(String numberPhone);
}
