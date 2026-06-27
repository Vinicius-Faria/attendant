package br.com.attendant.controller;

import br.com.attendant.entity.Agenda;
import br.com.attendant.service.AgendaService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/agenda", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin("*")
public class AgendaController extends BaseController<Agenda, Long, AgendaService> {
    public AgendaController(AgendaService agendaService) {
        super(agendaService);
    }
}
