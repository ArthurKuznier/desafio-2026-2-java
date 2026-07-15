package br.edu.unoesc.gestao_documentos.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PreUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class AuditoriaListener {

    private static final Logger log = LoggerFactory.getLogger(AuditoriaListener.class);

    private static AuditoriaRepository auditoriaRepository;
    private static ObjectMapper objectMapper;

    @Autowired
    public void setAuditoriaRepository(AuditoriaRepository auditoriaRepository) {
        AuditoriaListener.auditoriaRepository = auditoriaRepository;
    }

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        AuditoriaListener.objectMapper = objectMapper;
    }

    @PostPersist
    public void aoInserir(Object entidade) {
        registrar(entidade, TipoOperacao.INSERT, null, serializar(entidade));
    }

    @PreUpdate
    public void aoAtualizar(Object entidade) {
        registrar(entidade, TipoOperacao.UPDATE, null, serializar(entidade));
    }

    @PreRemove
    public void aoRemover(Object entidade) {
        registrar(entidade, TipoOperacao.DELETE, serializar(entidade), null);
    }

    private void registrar(Object entidade, TipoOperacao operacao, String estadoAnterior, String estadoNovo) {
        if (auditoriaRepository == null)
            return;
        try {
            Auditoria auditoria = new Auditoria();
            auditoria.setEntidade(entidade.getClass().getSimpleName());
            auditoria.setEntidadeId(extrairId(entidade));
            auditoria.setOperacao(operacao);
            auditoria.setEstadoAnterior(estadoAnterior);
            auditoria.setEstadoNovo(estadoNovo);
            auditoria.setUsuario(usuarioAtual());
            auditoria.setDataHora(LocalDateTime.now());
            auditoriaRepository.save(auditoria);
        } catch (Exception e) {
            log.error("Falha ao registrar auditoria para {}", entidade.getClass().getSimpleName(), e);
        }
    }

    private String extrairId(Object entidade) {
        try {
            Object id = entidade.getClass().getMethod("getId").invoke(entidade);
            return String.valueOf(id);
        } catch (Exception e) {
            return "desconhecido";
        }
    }

    private String serializar(Object entidade) {
        try {
            return objectMapper.writeValueAsString(entidade);
        } catch (Exception e) {
            return "{\"aviso\":\"nao foi possivel serializar o estado da entidade\"}";
        }
    }

    private String usuarioAtual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return auth.getName();
        }
        return "sistema";
    }
}