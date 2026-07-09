package br.com.attendant.controller;

import br.com.attendant.config.BusinessException;
import br.com.attendant.config.ExceptionEnum;
import br.com.attendant.entity.Enterprise;
import br.com.attendant.service.CodigoVerificacaoService;
import br.com.attendant.service.EnterpriseService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(value = "/codigo-verificacao", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin("*")
@Tag(name = "CodigoVerificacao", description = "Endpoints para Código de Verificaçao")
public class CodigoVerificacaoController {

    private final CodigoVerificacaoService codigoVerificacaoService;
    private final EnterpriseService enterpriseService;

    CodigoVerificacaoController(CodigoVerificacaoService codigoVerificacaoService, EnterpriseService enterpriseService) {
        this.codigoVerificacaoService = codigoVerificacaoService;
        this.enterpriseService = enterpriseService;
    }

    @GetMapping("/{email}")
    public ResponseEntity<?> enviaOrReenviaCodigo(@PathVariable String email) {
        Enterprise enterprise = enterpriseService.findEnterpriseByEmail(email);
        codigoVerificacaoService.enviarCodigo(enterprise);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Código Enviado"));
    }

    @GetMapping("/verifica/{email}/{codigo}")
    public ResponseEntity<?> validaCodigo(@PathVariable String email, @PathVariable String codigo) {
        Enterprise enterprise = enterpriseService.findEnterpriseByEmail(email);
        if (!codigoVerificacaoService.verificaCodigoByUser(codigo, enterprise)) {
            throw new BusinessException(ExceptionEnum.GENERIC, "Código não encontrado ou expirado");
        }
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of("message", "Codigo Verificado"));
    }

}
