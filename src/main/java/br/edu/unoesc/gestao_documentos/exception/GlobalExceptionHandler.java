package br.edu.unoesc.gestao_documentos.exception;

import br.edu.unoesc.gestao_documentos.dto.ErroResponseDto;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroResponseDto> handleRegraNegocio(RegraNegocioException ex) {
        ErroResponseDto erro = new ErroResponseDto(
                HttpStatus.BAD_REQUEST.value(),
                "Erro de Validação",
                ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erro);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErroResponseDto> handleNotFound(EntityNotFoundException ex) {
        ErroResponseDto erro = new ErroResponseDto(
                HttpStatus.NOT_FOUND.value(),
                "Recurso Não Encontrado",
                ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(erro);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponseDto> handleValidacaoDeCampos(MethodArgumentNotValidException ex) {
        String mensagem = ex.getBindingResult().getFieldErrors().stream()
                .map(erroDeCampo -> erroDeCampo.getField() + ": " + erroDeCampo.getDefaultMessage())
                .collect(Collectors.joining("; "));
        ErroResponseDto erro = new ErroResponseDto(
                HttpStatus.BAD_REQUEST.value(),
                "Erro de Validação de Campos",
                mensagem);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erro);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErroResponseDto> handleAutenticacao(AuthenticationException ex) {
        log.warn("Falha de autenticação: {}", ex.getMessage());
        ErroResponseDto erro = new ErroResponseDto(
                HttpStatus.UNAUTHORIZED.value(),
                "Não Autorizado",
                "Usuário ou senha inválidos");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(erro);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroResponseDto> handleIntegridade(DataIntegrityViolationException ex) {
        log.warn("Violação de integridade: {}", ex.getMessage());
        ErroResponseDto erro = new ErroResponseDto(
                HttpStatus.CONFLICT.value(),
                "Conflito de Integridade",
                "Não é possível concluir a operação: existem registros vinculados a este recurso");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(erro);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponseDto> handleErroInesperado(Exception ex) {
        log.error("Erro inesperado não tratado", ex);
        ErroResponseDto erro = new ErroResponseDto(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Erro Interno",
                "Ocorreu um erro inesperado. Tente novamente mais tarde.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(erro);
    }
}