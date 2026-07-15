package br.edu.unoesc.gestao_documentos.exception;

import br.edu.unoesc.gestao_documentos.dto.ErroResponseDto;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErroResponseDto> handleRegraNegocioException(RuntimeException ex) {
        ErroResponseDto erro = new ErroResponseDto(
                HttpStatus.BAD_REQUEST.value(),
                "Erro de Validação",
                ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erro);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErroResponseDto> handleNotFoundException(EntityNotFoundException ex) {
        ErroResponseDto erro = new ErroResponseDto(
                HttpStatus.NOT_FOUND.value(),
                "Recurso Não Encontrado",
                ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(erro);
    }
}