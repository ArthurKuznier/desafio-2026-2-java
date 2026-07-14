package br.edu.unoesc.gestao_documentos.service;

import org.springframework.stereotype.Service;
import br.edu.unoesc.gestao_documentos.repositories.SolicitacaoRepository;
import br.edu.unoesc.gestao_documentos.domain.Solicitacao;

@Service
public class SolicitacaoService {
    private final SolicitacaoRepository solicitacaoRepository;

    public SolicitacaoService(SolicitacaoRepository solicitacaoRepository) {
        this.solicitacaoRepository = solicitacaoRepository;
    }

    // Insert pelo JPA
    public Solicitacao criarSolicitacao(Solicitacao solicitacao) {
        return this.solicitacaoRepository.save(solicitacao);
    }
}
