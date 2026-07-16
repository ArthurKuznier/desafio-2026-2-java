package br.edu.unoesc.gestao_documentos.service;

import br.edu.unoesc.gestao_documentos.domain.Aluno;
import br.edu.unoesc.gestao_documentos.domain.Solicitacao;
import br.edu.unoesc.gestao_documentos.domain.Status;
import br.edu.unoesc.gestao_documentos.domain.StatusNome;
import br.edu.unoesc.gestao_documentos.exception.RegraNegocioException;
import br.edu.unoesc.gestao_documentos.repositories.AlunoRepository;
import br.edu.unoesc.gestao_documentos.repositories.SolicitacaoRepository;
import br.edu.unoesc.gestao_documentos.repositories.StatusRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.EnumSet;
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

    public Solicitacao criarSolicitacao(Solicitacao solicitacao) {
        Aluno aluno = buscarAlunoAtivo(solicitacao);
        solicitacao.setAluno(aluno);

        LocalDateTime agora = LocalDateTime.now();
        solicitacao.setDataSolicitacao(agora);
        solicitacao.setDataAlteracao(agora);
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

        if (!aluno.isAtivo()) {
            throw new RegraNegocioException(
                    "Aluno de id " + aluno.getId() + " esta inativo e nao pode abrir novas solicitacoes");
        }

        return aluno;
    }

    public Solicitacao alterarStatus(Integer solicitacaoId, Integer novoStatusId, Integer responsavelInformado) {
        Solicitacao solicitacao = solicitacaoRepository.findById(solicitacaoId)
                .orElseThrow(() -> new EntityNotFoundException("Solicitação não encontrada"));

        Status novoStatus = statusRepository.findById(novoStatusId)
                .orElseThrow(() -> new EntityNotFoundException("Status não encontrado"));

        if (novoStatus.getResponsavel() == null || !novoStatus.getResponsavel().equals(responsavelInformado)) {
            throw new RegraNegocioException("Responsavel Invalido");
        }

        StatusNome statusAtual = statusNomeDe(solicitacao.getStatus());
        StatusNome statusNovo = statusNomeDe(novoStatus);

        if (!transicaoPermitida(statusAtual, statusNovo)) {
            throw new RegraNegocioException(
                    "Transição inválida de " + statusAtual + " para " + statusNovo);
        }

        solicitacao.setStatus(novoStatus);
        solicitacao.setDataAlteracao(LocalDateTime.now());
        solicitacao.setDataEmissao(novoStatus.isFinalizaSolicitacao() ? LocalDateTime.now() : null);

        return solicitacaoRepository.save(solicitacao);
    }

    public Page<Solicitacao> buscarSolicitacoes(String nomeAluno, Integer cursoId, Pageable pageable) {
        return solicitacaoRepository.buscarComFiltros(nomeAluno, cursoId, pageable);
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