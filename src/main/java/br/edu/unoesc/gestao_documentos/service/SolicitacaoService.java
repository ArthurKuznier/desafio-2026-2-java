package br.edu.unoesc.gestao_documentos.service;

import br.edu.unoesc.gestao_documentos.domain.Solicitacao;
import br.edu.unoesc.gestao_documentos.domain.Status;
import br.edu.unoesc.gestao_documentos.repositories.SolicitacaoRepository;
import br.edu.unoesc.gestao_documentos.repositories.StatusRepository;
import org.springframework.stereotype.Service;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
public class SolicitacaoService {
    private final SolicitacaoRepository solicitacaoRepository;
    private final StatusRepository statusRepository;

    public SolicitacaoService(SolicitacaoRepository solicitacaoRepository, StatusRepository statusRepository) {
        this.solicitacaoRepository = solicitacaoRepository;
        this.statusRepository = statusRepository;
    }

    // Insert pelo JPA
    public Solicitacao criarSolicitacao(Solicitacao solicitacao) {
        return this.solicitacaoRepository.save(solicitacao);
    }

    public Solicitacao alterarStatus(Integer solicitacaoId, Integer novoStatusId, Integer responsavelInformado) {
        Solicitacao solicitacao = solicitacaoRepository.findById(solicitacaoId)
                .orElseThrow(() -> new EntityNotFoundException("Solicitação não encontrada"));

        Status novoStatus = statusRepository.findById(novoStatusId)
                .orElseThrow(() -> new EntityNotFoundException("Status não encontrado"));

        if (novoStatus.getResponsavel() != responsavelInformado) {
            throw new RuntimeException("Responsavel Invalido");
        }

        String statusAtualNome = solicitacao.getStatus().getNome().toUpperCase();
        String novoStatusNome = novoStatus.getNome().toUpperCase();

        if (!validarTransicao(statusAtualNome, novoStatusNome)) {
            throw new RuntimeException("Transição inválida de " + statusAtualNome + " para " + novoStatusNome);
        }

        solicitacao.setStatus(novoStatus);
        return solicitacaoRepository.save(solicitacao);
    }

    private boolean validarTransicao(String atual, String novo) {
        if (atual.equals("ABERTA") && novo.equals("EM_ANALISE"))
            return true;
        if (atual.equals("EM_ANALISE") && novo.equals("REPROVADA"))
            return true;
        if (atual.equals("EM_ANALISE") && novo.equals("APROVADA"))
            return true;
        if (atual.equals("APROVADA") && novo.equals("EMITIDA"))
            return true;

        return false;
    }

    public Page<Solicitacao> buscarSolicitacoes(String nomeAluno, Integer cursoId, Pageable pageable) {
        return solicitacaoRepository.buscarComFiltros(nomeAluno, cursoId, pageable);
    }
}