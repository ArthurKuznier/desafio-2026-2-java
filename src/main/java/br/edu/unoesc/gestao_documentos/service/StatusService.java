package br.edu.unoesc.gestao_documentos.service;

import br.edu.unoesc.gestao_documentos.domain.Status;
import br.edu.unoesc.gestao_documentos.repositories.StatusRepository;
import org.springframework.stereotype.Service;

@Service
public class StatusService extends AbstractCrudService<Status, Integer> {

    public StatusService(StatusRepository statusRepository) {
        super(statusRepository);
    }

    @Override
    public Status atualizar(Integer id, Status status) {
        if (status.getFinalizaSolicitacao() == null) {
            repository.findById(id)
                    .ifPresent(existente -> status.setFinalizaSolicitacao(existente.getFinalizaSolicitacao()));
        }
        return super.atualizar(id, status);
    }
}