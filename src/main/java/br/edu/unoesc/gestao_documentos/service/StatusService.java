package br.edu.unoesc.gestao_documentos.service;

import org.springframework.stereotype.Service;

import br.edu.unoesc.gestao_documentos.repositories.StatusRepository;
import br.edu.unoesc.gestao_documentos.domain.Status;

@Service
public class StatusService {
    private final StatusRepository statusRepository;

    public StatusService(StatusRepository statusRepository) {
        this.statusRepository = statusRepository;
    }

    public Status salvar(Status status) {
        return this.statusRepository.save(status);
    }
}
