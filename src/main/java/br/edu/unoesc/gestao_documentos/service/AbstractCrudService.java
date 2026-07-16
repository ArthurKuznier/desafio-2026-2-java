package br.edu.unoesc.gestao_documentos.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public abstract class AbstractCrudService<T, ID> {

    protected final JpaRepository<T, ID> repository;

    protected AbstractCrudService(JpaRepository<T, ID> repository) {
        this.repository = repository;
    }

    public T salvar(T entidade) {
        return repository.save(entidade);
    }

    public Page<T> listar(Pageable pageable) {
        return repository.findAll(pageable);
    }
}