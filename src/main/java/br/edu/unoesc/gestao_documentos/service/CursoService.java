package br.edu.unoesc.gestao_documentos.service;

import br.edu.unoesc.gestao_documentos.domain.Curso;
import br.edu.unoesc.gestao_documentos.repositories.CursoRepository;
import org.springframework.stereotype.Service;

@Service
public class CursoService extends AbstractCrudService<Curso, Integer> {

    public CursoService(CursoRepository cursoRepository) {
        super(cursoRepository);
    }
}
