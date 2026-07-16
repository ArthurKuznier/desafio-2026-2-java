package br.edu.unoesc.gestao_documentos.repositories;

import br.edu.unoesc.gestao_documentos.domain.Solicitacao;
import br.edu.unoesc.gestao_documentos.repositories.projection.DocumentoCountProjection;
import br.edu.unoesc.gestao_documentos.repositories.projection.PeriodoCountProjection;
import br.edu.unoesc.gestao_documentos.repositories.projection.StatusCountProjection;
import br.edu.unoesc.gestao_documentos.repositories.projection.TempoMedioEmissaoProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SolicitacaoRepository extends JpaRepository<Solicitacao, Integer> {

        @Query("SELECT s FROM Solicitacao s " +
                        "JOIN FETCH s.aluno a " +
                        "JOIN FETCH s.curso c " +
                        "JOIN FETCH s.tipo t " +
                        "JOIN FETCH s.status st " +
                        "WHERE (:nomeAluno IS NULL OR LOWER(a.nome) LIKE LOWER(CONCAT('%', :nomeAluno, '%'))) " +
                        "AND (:nomeCurso IS NULL OR LOWER(c.nome) LIKE LOWER(CONCAT('%', :nomeCurso, '%'))) " +
                        "AND (:nomeTipoDocumento IS NULL OR LOWER(t.nome) LIKE LOWER(CONCAT('%', :nomeTipoDocumento, '%'))) "
                        +
                        "AND (:statusId IS NULL OR st.id = :statusId) " +
                        "AND (:dataInicio IS NULL OR s.dataSolicitacao >= :dataInicio) " +
                        "AND (:dataFim IS NULL OR s.dataSolicitacao <= :dataFim)")
        Page<Solicitacao> buscarComFiltros(
                        @Param("nomeAluno") String nomeAluno,
                        @Param("nomeCurso") String nomeCurso,
                        @Param("nomeTipoDocumento") String nomeTipoDocumento,
                        @Param("statusId") Integer statusId,
                        @Param("dataInicio") LocalDateTime dataInicio,
                        @Param("dataFim") LocalDateTime dataFim,
                        Pageable pageable);

        @Query("SELECT s FROM Solicitacao s " +
                        "JOIN FETCH s.aluno " +
                        "JOIN FETCH s.curso " +
                        "JOIN FETCH s.tipo " +
                        "JOIN FETCH s.status " +
                        "WHERE s.id = :id")
        Optional<Solicitacao> buscarCompletoPorId(@Param("id") Integer id);

        Page<Solicitacao> findByAlunoId(Integer alunoId, Pageable pageable);

        @Query("SELECT s.status.nome AS status, COUNT(s) AS quantidade " +
                        "FROM Solicitacao s GROUP BY s.status.nome ORDER BY COUNT(s) DESC")
        List<StatusCountProjection> contarPorStatus();

        @Query(value = "SELECT CAST(data_solicitacao AS date) AS \"data\", COUNT(*) AS \"quantidade\" " +
                        "FROM solicitacao " +
                        "WHERE (CAST(:dataInicio AS timestamp) IS NULL OR data_solicitacao >= :dataInicio) " +
                        "AND (CAST(:dataFim AS timestamp) IS NULL OR data_solicitacao <= :dataFim) " +
                        "GROUP BY CAST(data_solicitacao AS date) ORDER BY CAST(data_solicitacao AS date)", nativeQuery = true)
        List<PeriodoCountProjection> contarPorPeriodo(
                        @Param("dataInicio") LocalDateTime dataInicio,
                        @Param("dataFim") LocalDateTime dataFim);

        @Query("SELECT s.tipo.nome AS tipoDocumento, COUNT(s) AS quantidade " +
                        "FROM Solicitacao s GROUP BY s.tipo.nome ORDER BY COUNT(s) DESC")
        List<DocumentoCountProjection> documentosMaisSolicitados();

        @Query(value = "SELECT COALESCE(AVG(EXTRACT(EPOCH FROM (data_emissao - data_solicitacao)) / 3600.0), 0) AS \"horasMedias\", "
                        +
                        "COUNT(*) AS \"total\" " +
                        "FROM solicitacao WHERE data_emissao IS NOT NULL", nativeQuery = true)
        TempoMedioEmissaoProjection calcularTempoMedioEmissao();
}