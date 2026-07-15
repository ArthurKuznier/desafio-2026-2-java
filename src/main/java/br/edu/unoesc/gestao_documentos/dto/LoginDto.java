package br.edu.unoesc.gestao_documentos.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginDto(
        @NotBlank(message = "O usuário é obrigatório") String username,

        @NotBlank(message = "A senha é obrigatória") String senha) {
}