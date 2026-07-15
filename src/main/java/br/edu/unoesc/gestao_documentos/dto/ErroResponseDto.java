package br.edu.unoesc.gestao_documentos.dto;

public record ErroResponseDto(
        Integer status,
        String erro,
        String mensagem) {
}