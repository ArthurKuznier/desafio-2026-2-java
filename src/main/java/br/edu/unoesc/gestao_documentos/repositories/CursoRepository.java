package br.edu.unoesc.gestao_documentos.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import br.edu.unoesc.gestao_documentos.domain.Curso;

@Repository
public interface CursoRepository extends JpaRepository<Curso, Integer> {
}
