package br.edu.unoesc.gestao_documentos.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class StatusNomeTest {

    @Test
    void deveNormalizarNomeComEspacoParaEnum() {
        assertThat(StatusNome.normalizar("Em Analise")).isEqualTo(StatusNome.EM_ANALISE);
    }

    @Test
    void deveNormalizarNomeComUnderlineEMinusculo() {
        assertThat(StatusNome.normalizar("em_analise")).isEqualTo(StatusNome.EM_ANALISE);
    }

    @Test
    void deveRetornarNuloParaNomeInvalido() {
        assertThat(StatusNome.normalizar("QUALQUER_COISA")).isNull();
    }

    @Test
    void deveRetornarNuloParaEntradaNula() {
        assertThat(StatusNome.normalizar(null)).isNull();
    }
}