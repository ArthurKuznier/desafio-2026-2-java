package br.edu.unoesc.gestao_documentos.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import br.edu.unoesc.gestao_documentos.domain.Aluno;

@Repository
public interface AlunoRepository extends JpaRepository<Aluno, Integer> {
}
