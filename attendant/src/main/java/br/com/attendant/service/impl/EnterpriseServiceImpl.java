package br.com.attendant.service.impl;

import br.com.attendant.config.BusinessException;
import br.com.attendant.config.ExceptionEnum;
import br.com.attendant.entity.Enterprise;
import br.com.attendant.repository.EnterpriseRepository;
import br.com.attendant.service.EnterpriseService;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
class EnterpriseServiceImpl extends BaseServiceImpl<Enterprise, Long, EnterpriseRepository> implements EnterpriseService {

    private PasswordEncoder passwordEncoder;

    EnterpriseServiceImpl(EnterpriseRepository enterpriseRepository,  PasswordEncoder passwordEncoder) {
        super(enterpriseRepository);
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Enterprise findEnterpriseByCnpj(String cnpj) {
        return repository.findByCnpj(cnpj);
    }

    @Override
    public Enterprise findEnterpriseByEmail(String email) {
        return repository.findByEmail(email);
    }

    @Override
    @Transactional
    public Enterprise save(Enterprise enterprise) {
        validate(enterprise);
        enterprise.setSenha(passwordEncoder.encode(enterprise.getSenha()));
        Enterprise enterpriseNew = repository.save(enterprise);
//        codigoVerificacaoService.enviarCodigo(usuarioNew);
//        cadastroBaseNovoUsuario(usuarioNew);
        return enterpriseNew;
    }

    @Override
    public void validate(Enterprise entity) {
        if (entity.getCnpj() == null || entity.getCnpj().isEmpty()) {
            throw new BusinessException(ExceptionEnum.ENTITY_INCOMPLETE, "É necessário informar o campo de CNPJ.");
        }

        if (entity.getDescricao() == null || entity.getDescricao().isEmpty()) {
            throw new BusinessException(ExceptionEnum.ENTITY_INCOMPLETE, "É necessário informar o campo de descrição.");
        }
    }
}
