package br.com.attendant.controller;

import br.com.attendant.entity.ServiceEnterprise;
import br.com.attendant.service.ServiceEnterpriseService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/service-enterprise", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin("*")
@Tag(name = "ServiceEnterprise", description = "Endpoints para gerenciamento dos Servios da empresa")
public class ServiceEnterpriseController extends BaseController<ServiceEnterprise, Long, ServiceEnterpriseService> {

    public ServiceEnterpriseController(ServiceEnterpriseService serviceEnterpriseService) {
        super(serviceEnterpriseService);
    }

}
