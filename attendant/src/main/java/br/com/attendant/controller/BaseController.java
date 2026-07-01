package br.com.attendant.controller;

import br.com.attendant.service.BaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import java.net.URI;
import java.util.Optional;

public abstract class BaseController<E, ID, S extends BaseService<E, ID>> {

    protected S service;

    public BaseController(S service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Criar um novo registro", description = "Insere um novo registro no sistema.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Registro criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados da requisição inválidos ou falha na validação")
    })
    public ResponseEntity<E> save(@RequestBody E entity) {
        E savedEntity = service.save(entity);
        try {
            Long id = (Long) savedEntity.getClass().getMethod("getId").invoke(savedEntity);
            final URI uri = MvcUriComponentsBuilder.fromController(getClass()).path("/{id}").buildAndExpand(id).toUri();
            return ResponseEntity.created(uri).body(savedEntity);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CREATED).body(savedEntity);
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar por ID", description = "Retorna um único registro baseado no ID fornecido.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Registro encontrado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Registro não encontrado")
    })
    public ResponseEntity<E> findById(
            @Parameter(description = "ID do registro a ser buscado", required = true, example = "1")
            @PathVariable ID id) {
        Optional<E> entity = service.findById(id);
        return entity.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar um registro existente", description = "Atualiza os dados de um registro baseado no ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Registro atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Registro não encontrado para atualização")
    })
    public ResponseEntity<E> update(
            @RequestBody E entity,
            @Parameter(description = "ID do registro a ser atualizado", required = true, example = "1")
            @PathVariable ID id) throws Exception {
        E updatedEntity = service.update(entity, id);
        return ResponseEntity.ok(updatedEntity);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Excluir um registro", description = "Remove permanentemente um registro do sistema baseado no ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "24", description = "Registro excluído com sucesso (Sem Conteúdo)"),
            @ApiResponse(responseCode = "404", description = "Registro não encontrado")
    })
    public void delete(
            @Parameter(description = "ID do registro a ser excluído", required = true, example = "1")
            @PathVariable ID id) {
        service.delete(id);
    }
}