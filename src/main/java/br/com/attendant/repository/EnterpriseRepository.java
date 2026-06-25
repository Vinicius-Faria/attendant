package br.com.attendant.repository;

import br.com.attendant.entity.Enterprise;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnterpriseRepository extends JpaRepository<Enterprise, Long> {

    Enterprise findByCnpj(String cnpj);

}
