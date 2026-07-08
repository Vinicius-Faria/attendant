package br.com.attendant.repository;

import br.com.attendant.entity.CodigoVerificacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CodigoVerificacaoRepository extends JpaRepository<CodigoVerificacao, Long> {
    Optional<CodigoVerificacao> findByEmail(String email);
}
