package br.com.attendant.service.impl;

import br.com.attendant.config.BusinessException;
import br.com.attendant.config.ExceptionEnum;
import br.com.attendant.entity.CodigoVerificacao;
import br.com.attendant.entity.Enterprise;
import br.com.attendant.repository.CodigoVerificacaoRepository;
import br.com.attendant.service.CodigoVerificacaoService;
import br.com.attendant.service.EnterpriseService;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Random;

@Service
class CodigoVerificacaoServiceImpl implements CodigoVerificacaoService {

    private final CodigoVerificacaoRepository codigoVerificacaoRepository;
    private final EnterpriseService enterpriseService;

    CodigoVerificacaoServiceImpl(CodigoVerificacaoRepository codigoVerificacaoRepository,  EnterpriseService enterpriseService) {
        this.codigoVerificacaoRepository = codigoVerificacaoRepository;
        this.enterpriseService = enterpriseService;
    }

    @Override
    public void enviarCodigo(Enterprise enterprise) {
        boolean codigoVerificacaoExist = codigoVerificacaoRepository.findByEmail(enterprise.getEmail()).isPresent();
        if (codigoVerificacaoExist) {
            reenviarCodigo(enterprise.getEmail());
            return;
        }
        CodigoVerificacao codigoVerificacao = new CodigoVerificacao();
        codigoVerificacao.setEmail(enterprise.getEmail());
        codigoVerificacao.setCodigo(gerarCodigo());

        saveAndSend(codigoVerificacao);
    }

    @Override
    public void reenviarCodigo(String email) {
        CodigoVerificacao codigoVerificacao =
                codigoVerificacaoRepository
                        .findByEmail(email)
                        .orElseThrow(() -> new BusinessException(ExceptionEnum.NOT_FOUND, "Nenhum código encontrado para este email."));

        if (codigoVerificacao.getAtualizadoEm() != null && codigoVerificacao.getAtualizadoEm().isAfter(LocalDateTime.now().minusSeconds(60))) {
            long segundos = Duration.between(codigoVerificacao.getAtualizadoEm(), LocalDateTime.now().minusSeconds(60)).getSeconds();
            throw new BusinessException(ExceptionEnum.GENERIC, "Aguarde " + segundos + " segundos antes de reenviar o código.");
        }

        codigoVerificacao.setCodigo(gerarCodigo());
        saveAndSend(codigoVerificacao);
    }

    @Override
    public boolean verificaCodigoByUser(String codigo, Enterprise enterprise) {
        return codigoVerificacaoRepository
                .findByEmail(enterprise.getEmail())
                .filter(cv -> cv.getCodigo().equals(codigo))
                .filter(cv -> cv.getAtualizadoEm().plusMinutes(15).isAfter(LocalDateTime.now()))
                .map(
                        cv -> {
                            enterprise.setEmailValido(true);
                            // TODO - Ver como tirar esse try/Catch depois
                            try {
                                enterpriseService.update(enterprise, enterprise.getId());
                            } catch (Exception e) {
                                throw new BusinessException(ExceptionEnum.GENERIC, "Erro ao atualizar os dados do usuário.");
                            }

                            codigoVerificacaoRepository.deleteById(cv.getId());
                            return true;
                        })
                .orElse(false);
    }

    private void saveAndSend(CodigoVerificacao codigoVerificacao) {
        codigoVerificacaoRepository.save(codigoVerificacao);
        System.out.println("Enviando o codigo: " + codigoVerificacao.getCodigo());
//        emailService.enviaEmailCodigoVerificacao(codigoVerificacao.getEmail(), codigoVerificacao.getCodigo());
    }

    private String gerarCodigo() {
        Random random = new Random();
        return String.valueOf(100000 + random.nextInt(900000)); // 6 digitos
    }

}
