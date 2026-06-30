package br.com.attendant.repository;

import br.com.attendant.entity.Enterprise;
import br.com.attendant.entity.ServiceEnterprise;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceEnterpriseRepository extends JpaRepository<ServiceEnterprise, Long> {

    List<ServiceEnterprise> findByEnterprise(Enterprise enterprise);

}
