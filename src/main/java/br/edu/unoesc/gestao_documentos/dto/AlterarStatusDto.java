package br.edu.unoesc.gestao_documentos.dto;

import jakarta.validation.constraints.NotNull;

public record AlterarStatusDto(
        @NotNull(message = "O ID do novo status é obrigatório") Integer statusId,
        @NotNull(message = "O responsável informado é obrigatório") Integer responsavel) {
}