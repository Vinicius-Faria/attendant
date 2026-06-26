package br.com.attendant.service;

import java.util.Optional;

public interface BaseService<E, ID> {
    E save(E e);

    E update(E e, ID id) throws Exception;

    void delete(ID id);

    Optional<E> findById(ID id);

    void validate(E e);
}
