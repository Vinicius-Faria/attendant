package br.com.attendant.repository;

import br.com.attendant.entity.ServiceEnterprise;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceEnterpriseRepository extends JpaRepository<ServiceEnterprise, Long> {
}
