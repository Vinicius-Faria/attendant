package br.com.attendant.controller;

import br.com.attendant.entity.Enterprise;
import br.com.attendant.service.EnterpriseService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/enterprise", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin("*")
@Tag(name = "Enterprise", description = "Endpoints para gerenciamento da Empresa")
public class EnterpriseController extends BaseController<Enterprise, Long, EnterpriseService> {

    public EnterpriseController(EnterpriseService enterpriseService) {
        super(enterpriseService);
    }

}
