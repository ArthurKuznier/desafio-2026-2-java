package br.edu.unoesc.gestao_documentos.audit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {
    Page<Auditoria> findByEntidadeOrderByDataHoraDesc(String entidade, Pageable pageable);

    Page<Auditoria> findByEntidadeAndEntidadeIdOrderByDataHoraDesc(String entidade, String entidadeId,
            Pageable pageable);
}