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

@Repository
public interface SolicitacaoRepository extends JpaRepository<Solicitacao, Integer> {

        // RF02 - filtros: Aluno.nome, Curso.nome, status, periodo, tipoDocumento.nome +
        // paginacao
        @Query("SELECT s FROM Solicitacao s WHERE " +
                        "(:nomeAluno IS NULL OR LOWER(s.aluno.nome) LIKE LOWER(CONCAT('%', :nomeAluno, '%'))) " +
                        "AND (:nomeCurso IS NULL OR LOWER(s.curso.nome) LIKE LOWER(CONCAT('%', :nomeCurso, '%'))) " +
                        "AND (:nomeTipoDocumento IS NULL OR LOWER(s.tipo.nome) LIKE LOWER(CONCAT('%', :nomeTipoDocumento, '%'))) "
                        +
                        "AND (:statusId IS NULL OR s.status.id = :statusId) " +
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

        // RF02 - "Solicitacoes realizadas por um Aluno"
        Page<Solicitacao> findByAlunoId(Integer alunoId, Pageable pageable);

        // RF02/RF06 - Quantidade de solicitacoes por Status
        @Query("SELECT s.status.nome AS status, COUNT(s) AS quantidade " +
                        "FROM Solicitacao s GROUP BY s.status.nome ORDER BY COUNT(s) DESC")
        List<StatusCountProjection> contarPorStatus();

        // RF02/RF06 - Quantidade de solicitacoes por periodo (dia)
        @Query(value = "SELECT CAST(data_solicitacao AS date) AS \"data\", COUNT(*) AS \"quantidade\" " +
                        "FROM solicitacao " +
                        "WHERE (CAST(:dataInicio AS timestamp) IS NULL OR data_solicitacao >= :dataInicio) " +
                        "AND (CAST(:dataFim AS timestamp) IS NULL OR data_solicitacao <= :dataFim) " +
                        "GROUP BY CAST(data_solicitacao AS date) ORDER BY CAST(data_solicitacao AS date)", nativeQuery = true)
        List<PeriodoCountProjection> contarPorPeriodo(
                        @Param("dataInicio") LocalDateTime dataInicio,
                        @Param("dataFim") LocalDateTime dataFim);

        // RF02/RF06 - Documentos mais solicitados
        @Query("SELECT s.tipo.nome AS tipoDocumento, COUNT(s) AS quantidade " +
                        "FROM Solicitacao s GROUP BY s.tipo.nome ORDER BY COUNT(s) DESC")
        List<DocumentoCountProjection> documentosMaisSolicitados();

        // RF02/RF06 - Media de tempo ate emissao (em horas), considerando so as ja
        // emitidas
        @Query(value = "SELECT COALESCE(AVG(EXTRACT(EPOCH FROM (data_emissao - data_solicitacao)) / 3600.0), 0) AS \"horasMedias\", "
                        +
                        "COUNT(*) AS \"total\" " +
                        "FROM solicitacao WHERE data_emissao IS NOT NULL", nativeQuery = true)
        TempoMedioEmissaoProjection calcularTempoMedioEmissao();
}