package br.edu.unoesc.gestao_documentos.repositories.projection;

import java.time.LocalDate;

public interface PeriodoCountProjection {
    LocalDate getData();

    Long getQuantidade();
}