package br.edu.unoesc.gestao_documentos.service;

import br.edu.unoesc.gestao_documentos.domain.Aluno;
import br.edu.unoesc.gestao_documentos.repositories.AlunoRepository;
import org.springframework.stereotype.Service;

@Service
public class AlunoService extends AbstractCrudService<Aluno, Integer> {

    public AlunoService(AlunoRepository alunoRepository) {
        super(alunoRepository);
    }
}
