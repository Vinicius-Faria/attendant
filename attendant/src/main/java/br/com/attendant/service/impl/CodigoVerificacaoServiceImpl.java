package br.com.attendant.service.impl;

import br.com.attendant.config.BusinessException;
import br.com.attendant.config.ExceptionEnum;
import br.com.attendant.entity.CodigoVerificacao;
import br.com.attendant.entity.Enterprise;
import br.com.attendant.repository.CodigoVerificacaoRepository;
import br.com.attendant.service.CodigoVerificacaoService;
import br.com.attendant.service.EnterpriseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
class CodigoVerificacaoServiceImpl implements CodigoVerificacaoService {

    private final CodigoVerificacaoRepository codigoVerificacaoRepository;
     @Autowired @Lazy private EnterpriseService enterpriseService;
    private final SecureRandom secureRandom = new SecureRandom(); // Mais seguro que Random

    CodigoVerificacaoServiceImpl(CodigoVerificacaoRepository codigoVerificacaoRepository) {
        this.codigoVerificacaoRepository = codigoVerificacaoRepository;
    }

    @Override
    @Transactional
    public void enviarCodigo(Enterprise enterprise) {
        var codigoExistenteOpt = codigoVerificacaoRepository.findByEmail(enterprise.getEmail());

        if (codigoExistenteOpt.isPresent()) {
            CodigoVerificacao codigoExistente = codigoExistenteOpt.get();

            if (codigoExistente.getAtualizadoEm().plusMinutes(15).isAfter(LocalDateTime.now())) {
                validarThrottling(codigoExistente);
                saveAndSend(codigoExistente);
                return;
            }
            codigoVerificacaoRepository.delete(codigoExistente);
        }

        CodigoVerificacao novoCodigo = new CodigoVerificacao();
        novoCodigo.setEmail(enterprise.getEmail());
        novoCodigo.setCodigo(gerarCodigo());

        saveAndSend(novoCodigo);
    }

    @Override
    @Transactional
    public void reenviarCodigo(String email) {
        CodigoVerificacao codigoVerificacao = codigoVerificacaoRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ExceptionEnum.NOT_FOUND, "Nenhum código encontrado para este email."));

        validarThrottling(codigoVerificacao);
        codigoVerificacao.setCodigo(gerarCodigo());
        saveAndSend(codigoVerificacao);
    }

    @Override
    @Transactional(noRollbackFor = BusinessException.class)
    public boolean verificaCodigoByUser(String codigo, Enterprise enterprise) {
        CodigoVerificacao cv = codigoVerificacaoRepository.findByEmail(enterprise.getEmail())
                .orElseThrow(() -> new BusinessException(ExceptionEnum.NOT_FOUND, "Código não encontrado ou já utilizado."));

        if (!cv.getCodigo().equals(codigo)) {
            throw new BusinessException(ExceptionEnum.GENERIC, "Código de verificação incorreto.");
        }

        if (cv.getAtualizadoEm().plusMinutes(15).isBefore(LocalDateTime.now())) {
            codigoVerificacaoRepository.delete(cv);
            reenviarCodigo(enterprise.getEmail());
            throw new BusinessException(ExceptionEnum.GENERIC, "Este código já expirou. Enviado novo código.");
        }

        enterprise.setEmailValido(true);
        try {
            enterpriseService.update(enterprise, enterprise.getId());
        } catch (Exception e) {
            throw new BusinessException(ExceptionEnum.GENERIC, e.getMessage());
        }

        codigoVerificacaoRepository.delete(cv);
        return true;
    }

    private void validarThrottling(CodigoVerificacao codigoVerificacao) {
        LocalDateTime permitirApos = codigoVerificacao.getAtualizadoEm().plusSeconds(60);
        if (LocalDateTime.now().isBefore(permitirApos)) {
            long segundosRestantes = Duration.between(LocalDateTime.now(), permitirApos).getSeconds();
            throw new BusinessException(ExceptionEnum.GENERIC, "Aguarde " + segundosRestantes + " segundos antes de solicitar um novo envio.");
        }
    }

    private void saveAndSend(CodigoVerificacao codigoVerificacao) {
        codigoVerificacao.setAtualizadoEm(LocalDateTime.now());
        codigoVerificacaoRepository.save(codigoVerificacao);

        System.out.println("Enviando o codigo: " + codigoVerificacao.getCodigo());
        // emailService.enviaEmailCodigoVerificacao(codigoVerificacao.getEmail(), codigoVerificacao.getCodigo());
    }

    private String gerarCodigo() {
        return String.valueOf(100000 + secureRandom.nextInt(900000));
    }
}