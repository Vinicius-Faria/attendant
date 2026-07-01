package br.com.attendant.controller;

import br.com.attendant.entity.Agenda;
import br.com.attendant.entity.Enterprise;
import br.com.attendant.service.AgendaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping(value = "/agenda", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin("*")
@Tag(name = "Agenda", description = "Endpoints para gerenciamento de agendamentos dos clientes")
public class AgendaController extends BaseController<Agenda, Long, AgendaService> {

    public AgendaController(AgendaService agendaService) {
        super(agendaService);
    }

    @GetMapping(value = "/find-date-enteprise", consumes = MediaType.ALL_VALUE)
    @Operation(
            summary = "Pega os agendamentos do dia e por empresa",
            description = "Pesquise pelo dia no formato YYYY-MM-DD e passe os atributos da empresa (como id) via parâmetros de URL."
    )
    public List<Agenda> findByDateEnteprise(
            @Parameter(description = "Data da consulta da agenda", example = "2026-07-01", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,

            @Parameter(description = "Empresa referente (informe o id da empresa)", example = "1", required = true)
            @RequestParam Enterprise enterprise) {

        return service.findByDateAndEnterprise(date, enterprise);
    }
}