package br.edu.unoesc.gestao_documentos.repositories.projection;

public interface DocumentoCountProjection {
    String getTipoDocumento();

    Long getQuantidade();
}