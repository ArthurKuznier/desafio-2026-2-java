package br.edu.unoesc.gestao_documentos.service;

import br.edu.unoesc.gestao_documentos.domain.Aluno;
import br.edu.unoesc.gestao_documentos.domain.Role;
import br.edu.unoesc.gestao_documentos.domain.Solicitacao;
import br.edu.unoesc.gestao_documentos.domain.Status;
import br.edu.unoesc.gestao_documentos.domain.StatusNome;
import br.edu.unoesc.gestao_documentos.domain.Usuario;
import br.edu.unoesc.gestao_documentos.exception.RegraNegocioException;
import br.edu.unoesc.gestao_documentos.repositories.AlunoRepository;
import br.edu.unoesc.gestao_documentos.repositories.SolicitacaoRepository;
import br.edu.unoesc.gestao_documentos.repositories.StatusRepository;
import br.edu.unoesc.gestao_documentos.repositories.projection.DocumentoCountProjection;
import br.edu.unoesc.gestao_documentos.repositories.projection.PeriodoCountProjection;
import br.edu.unoesc.gestao_documentos.repositories.projection.StatusCountProjection;
import br.edu.unoesc.gestao_documentos.repositories.projection.TempoMedioEmissaoProjection;
import br.edu.unoesc.gestao_documentos.repositories.specification.SolicitacaoSpecification;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class SolicitacaoService {

    private static final Map<StatusNome, Set<StatusNome>> TRANSICOES_VALIDAS = new EnumMap<>(StatusNome.class);
    static {
        TRANSICOES_VALIDAS.put(StatusNome.ABERTA, EnumSet.of(StatusNome.EM_ANALISE));
        TRANSICOES_VALIDAS.put(StatusNome.EM_ANALISE, EnumSet.of(StatusNome.APROVADA, StatusNome.REPROVADA));
        TRANSICOES_VALIDAS.put(StatusNome.APROVADA, EnumSet.of(StatusNome.EMITIDA));
    }

    private final SolicitacaoRepository solicitacaoRepository;
    private final StatusRepository statusRepository;
    private final AlunoRepository alunoRepository;

    public SolicitacaoService(SolicitacaoRepository solicitacaoRepository, StatusRepository statusRepository,
            AlunoRepository alunoRepository) {
        this.solicitacaoRepository = solicitacaoRepository;
        this.statusRepository = statusRepository;
        this.alunoRepository = alunoRepository;
    }

    @Transactional
    public Solicitacao criarSolicitacao(Solicitacao solicitacao) {
        Aluno aluno = buscarAlunoAtivo(solicitacao);
        solicitacao.setAluno(aluno);

        LocalDateTime agora = LocalDateTime.now();
        solicitacao.setDataSolicitacao(agora);
        solicitacao.setDataAlteracao(agora);
        solicitacao.setUltimaAtualizacaoPor(usuarioAtual());
        if (solicitacao.getStatus() == null) {
            solicitacao.setStatus(buscarOuCriarStatusAberta());
        }
        return solicitacaoRepository.save(solicitacao);
    }

    private Aluno buscarAlunoAtivo(Solicitacao solicitacao) {
        if (solicitacao.getAluno() == null || solicitacao.getAluno().getId() == null) {
            throw new RegraNegocioException("O id do aluno e obrigatorio");
        }

        Aluno aluno = alunoRepository.findById(solicitacao.getAluno().getId())
                .orElseThrow(() -> new EntityNotFoundException("Aluno nao encontrado"));

        if (!aluno.getAtivo()) {
            throw new RegraNegocioException(
                    "Aluno de id " + aluno.getId() + " esta inativo e nao pode abrir novas solicitacoes");
        }

        return aluno;
    }

    @Transactional
    public Solicitacao alterarStatus(Integer solicitacaoId, Integer novoStatusId) {
        Solicitacao solicitacao = solicitacaoRepository.buscarCompletoPorId(solicitacaoId)
                .orElseThrow(() -> new EntityNotFoundException("Solicitação não encontrada"));

        Status novoStatus = statusRepository.findById(novoStatusId)
                .orElseThrow(() -> new EntityNotFoundException("Status não encontrado"));

        Usuario usuarioLogado = usuarioAutenticado();
        validarResponsavelPeloStatus(usuarioLogado, novoStatus);

        StatusNome statusAtual = statusNomeDe(solicitacao.getStatus());
        StatusNome statusNovo = statusNomeDe(novoStatus);

        if (!transicaoPermitida(statusAtual, statusNovo)) {
            throw new RegraNegocioException(
                    "Transição inválida de " + statusAtual + " para " + statusNovo);
        }

        solicitacao.setStatus(novoStatus);
        solicitacao.setDataAlteracao(LocalDateTime.now());
        solicitacao.setDataEmissao(statusNovo == StatusNome.EMITIDA ? LocalDateTime.now() : null);
        solicitacao.setUltimaAtualizacaoPor(usuarioLogado.getUsername());

        return solicitacaoRepository.save(solicitacao);
    }

    private void validarResponsavelPeloStatus(Usuario usuarioLogado, Status novoStatus) {
        if (usuarioLogado.getRole() == Role.ADMIN) {
            return;
        }
        if (novoStatus.getResponsavel() == null
                || usuarioLogado.getCodigoResponsavel() == null
                || !novoStatus.getResponsavel().equals(usuarioLogado.getCodigoResponsavel())) {
            throw new RegraNegocioException("Você não é responsável por esta etapa do fluxo");
        }
    }

    private String usuarioAtual() {
        Usuario usuario = usuarioAutenticadoOuNull();
        return usuario != null ? usuario.getUsername() : "sistema";
    }

    private Usuario usuarioAutenticado() {
        Usuario usuario = usuarioAutenticadoOuNull();
        if (usuario == null) {
            throw new RegraNegocioException("Usuário não autenticado");
        }
        return usuario;
    }

    private Usuario usuarioAutenticadoOuNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof Usuario usuario) {
            return usuario;
        }
        return null;
    }

    public Page<Solicitacao> buscarSolicitacoes(String nomeAluno, String nomeCurso, String nomeTipoDocumento,
            Integer statusId, LocalDateTime dataInicio, LocalDateTime dataFim, Pageable pageable) {
        Specification<Solicitacao> filtros = SolicitacaoSpecification.comFiltros(
                nomeAluno, nomeCurso, nomeTipoDocumento, statusId, dataInicio, dataFim);
        return solicitacaoRepository.findAll(filtros, pageable);
    }

    public Page<Solicitacao> buscarPorAluno(Integer alunoId, Pageable pageable) {
        return solicitacaoRepository.findByAlunoId(alunoId, pageable);
    }

    public List<StatusCountProjection> estatisticasPorStatus() {
        return solicitacaoRepository.contarPorStatus();
    }

    public List<PeriodoCountProjection> estatisticasPorPeriodo(LocalDateTime dataInicio, LocalDateTime dataFim) {
        return solicitacaoRepository.contarPorPeriodo(dataInicio, dataFim);
    }

    public List<DocumentoCountProjection> documentosMaisSolicitados() {
        return solicitacaoRepository.documentosMaisSolicitados();
    }

    public TempoMedioEmissaoProjection tempoMedioEmissao() {
        return solicitacaoRepository.calcularTempoMedioEmissao();
    }

    private Status buscarOuCriarStatusAberta() {
        return statusRepository.findByNomeIgnoreCase(StatusNome.ABERTA.name())
                .orElseGet(() -> {
                    Status status = new Status();
                    status.setNome(StatusNome.ABERTA.name());
                    status.setResponsavel(1);
                    status.setFinalizaSolicitacao(false);
                    return statusRepository.save(status);
                });
    }

    private boolean transicaoPermitida(StatusNome atual, StatusNome novo) {
        return TRANSICOES_VALIDAS.getOrDefault(atual, EnumSet.noneOf(StatusNome.class)).contains(novo);
    }

    private StatusNome statusNomeDe(Status status) {
        StatusNome nome = StatusNome.normalizar(status.getNome());
        if (nome == null) {
            throw new RegraNegocioException("Status desconhecido: " + status.getNome());
        }
        return nome;
    }
}