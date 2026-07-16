package br.edu.unoesc.gestao_documentos.repositories.projection;

public interface StatusCountProjection {
    String getStatus();

    Long getQuantidade();
}