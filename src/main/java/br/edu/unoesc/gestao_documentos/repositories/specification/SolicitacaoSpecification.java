package br.edu.unoesc.gestao_documentos.repositories.specification;

import br.edu.unoesc.gestao_documentos.domain.Solicitacao;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SolicitacaoSpecification {

    private SolicitacaoSpecification() {
    }

    public static Specification<Solicitacao> comFiltros(
            String nomeAluno,
            String nomeCurso,
            String nomeTipoDocumento,
            Integer statusId,
            LocalDateTime dataInicio,
            LocalDateTime dataFim) {

        return (root, query, cb) -> {
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("aluno", JoinType.LEFT);
                root.fetch("curso", JoinType.LEFT);
                root.fetch("tipo", JoinType.LEFT);
                root.fetch("status", JoinType.LEFT);
            }

            List<Predicate> predicados = new ArrayList<>();

            if (nomeAluno != null && !nomeAluno.isBlank()) {
                predicados.add(cb.like(cb.lower(root.get("aluno").get("nome")),
                        "%" + nomeAluno.toLowerCase() + "%"));
            }
            if (nomeCurso != null && !nomeCurso.isBlank()) {
                predicados.add(cb.like(cb.lower(root.get("curso").get("nome")),
                        "%" + nomeCurso.toLowerCase() + "%"));
            }
            if (nomeTipoDocumento != null && !nomeTipoDocumento.isBlank()) {
                predicados.add(cb.like(cb.lower(root.get("tipo").get("nome")),
                        "%" + nomeTipoDocumento.toLowerCase() + "%"));
            }
            if (statusId != null) {
                predicados.add(cb.equal(root.get("status").get("id"), statusId));
            }
            if (dataInicio != null) {
                predicados.add(cb.greaterThanOrEqualTo(root.get("dataSolicitacao"), dataInicio));
            }
            if (dataFim != null) {
                predicados.add(cb.lessThanOrEqualTo(root.get("dataSolicitacao"), dataFim));
            }

            return cb.and(predicados.toArray(new Predicate[0]));
        };
    }
}