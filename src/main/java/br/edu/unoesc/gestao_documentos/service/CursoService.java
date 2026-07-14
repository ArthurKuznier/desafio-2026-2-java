package br.edu.unoesc.gestao_documentos.service;

import org.springframework.stereotype.Service;

import br.edu.unoesc.gestao_documentos.repositories.CursoRepository;
import br.edu.unoesc.gestao_documentos.domain.Curso;

@Service
public class CursoService {
    private final CursoRepository cursoRepository;

    public CursoService(CursoRepository cursoRepository) {
        this.cursoRepository = cursoRepository;
    }

    public Curso salvar(Curso curso) {
        return this.cursoRepository.save(curso);
    }
}
