package br.edu.unoesc.gestao_documentos.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import br.edu.unoesc.gestao_documentos.domain.Solicitacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

@Repository
public interface SolicitacaoRepository extends JpaRepository<Solicitacao, Integer> {

        @Query("SELECT s FROM Solicitacao s WHERE " +
                        "(:nomeAluno IS NULL OR LOWER(s.aluno.nome) LIKE LOWER(CONCAT('%', :nomeAluno, '%'))) " +
                        "AND (:cursoId IS NULL OR s.curso.id = :cursoId)")
        Page<Solicitacao> buscarComFiltros(
                        @Param("nomeAluno") String nomeAluno,
                        @Param("cursoId") Integer cursoId,
                        Pageable pageable);
}
