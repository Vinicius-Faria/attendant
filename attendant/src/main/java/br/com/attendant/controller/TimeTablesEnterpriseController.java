package br.com.attendant.controller;

import br.com.attendant.entity.TimeTablesEnterprise;
import br.com.attendant.service.TimeTablesEnterpriseService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/time-table", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin("*")
@Tag(name = "TimeTablesEnterprise", description = "Endpoints para gerenciamento do horário de funcionamento da empresa")
public class TimeTablesEnterpriseController extends BaseController<TimeTablesEnterprise, Long, TimeTablesEnterpriseService> {

    public TimeTablesEnterpriseController(TimeTablesEnterpriseService timeTablesEnterpriseService) {
        super(timeTablesEnterpriseService);
    }

}