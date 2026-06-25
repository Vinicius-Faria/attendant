package br.com.attendant.controller;

import br.com.attendant.service.BaseService;
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
    public ResponseEntity<E> findById(@PathVariable ID id) {
        Optional<E> entity = service.findById(id);
        return entity.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<E> update(@RequestBody E entity, @PathVariable ID id) throws Exception {
        E updatedEntity = service.update(entity, id);
        return ResponseEntity.ok(updatedEntity);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable ID id) {
        service.delete(id);
    }
}
