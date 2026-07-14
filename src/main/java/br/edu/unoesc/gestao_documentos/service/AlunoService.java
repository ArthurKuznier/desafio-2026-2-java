package br.edu.unoesc.gestao_documentos.service;

import org.springframework.stereotype.Service;

import br.edu.unoesc.gestao_documentos.repositories.AlunoRepository;
import br.edu.unoesc.gestao_documentos.domain.Aluno;

@Service
public class AlunoService {
    private final AlunoRepository alunoRepository;

    public AlunoService(AlunoRepository alunoRepository) {
        this.alunoRepository = alunoRepository;
    }

    public Aluno salvar(Aluno aluno) {
        return this.alunoRepository.save(aluno);
    }
}
