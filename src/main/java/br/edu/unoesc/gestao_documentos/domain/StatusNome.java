package br.edu.unoesc.gestao_documentos.domain;

public enum StatusNome {
    ABERTA,
    EM_ANALISE,
    APROVADA,
    REPROVADA,
    EMITIDA;

    public static StatusNome normalizar(String nome) {
        if (nome == null) {
            return null;
        }
        String chave = nome.trim().toUpperCase().replace(" ", "_");
        try {
            return StatusNome.valueOf(chave);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}