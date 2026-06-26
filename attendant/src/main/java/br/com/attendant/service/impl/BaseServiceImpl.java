package br.com.attendant.service.impl;

import br.com.attendant.config.BusinessException;
import br.com.attendant.config.ExceptionEnum;
import br.com.attendant.config.PropertiesNames;
import br.com.attendant.service.BaseService;
import org.springframework.beans.BeanUtils;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

abstract class BaseServiceImpl<E, ID, R extends JpaRepository<E, ID>> implements BaseService<E, ID> {

    protected final R repository;

    protected BaseServiceImpl(R repository) {
        this.repository = repository;
    }

    @Override
    public E save(E entity) {
        validate(entity);
        return repository.save(entity);
    }

    @Override
    public E update(E entity, ID id) throws Exception {
        Optional<E> existingEntityOptional = findById(id);
        if (existingEntityOptional.isEmpty()) {
            throw new BusinessException(ExceptionEnum.NOT_FOUND, "Entity with id " + id + " not found.");
        }

        E existingEntity = existingEntityOptional.get();
        BeanUtils.copyProperties(entity, existingEntity, PropertiesNames.getNullPropertyNames(entity));

        validate(existingEntity);
        return repository.save(existingEntity);
    }

    @Override
    public void delete(ID id) {
        repository.deleteById(id);
    }

    @Override
    public Optional<E> findById(ID id) {
        return repository.findById(id);
    }
}
