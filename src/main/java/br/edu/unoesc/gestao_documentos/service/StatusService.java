package br.edu.unoesc.gestao_documentos.service;

import br.edu.unoesc.gestao_documentos.domain.Status;
import br.edu.unoesc.gestao_documentos.repositories.StatusRepository;
import org.springframework.stereotype.Service;

@Service
public class StatusService extends AbstractCrudService<Status, Integer> {

    public StatusService(StatusRepository statusRepository) {
        super(statusRepository);
    }
}
