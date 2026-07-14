package br.edu.unoesc.gestao_documentos.controller;

import br.edu.unoesc.gestao_documentos.service.SolicitacaoService;
import br.edu.unoesc.gestao_documentos.domain.Solicitacao;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/solicitacoes")
public class SolicitacaoController {

    private final SolicitacaoService solicitacaoService;

    public SolicitacaoController(SolicitacaoService solicitacaoService) {
        this.solicitacaoService = solicitacaoService;
    }

    @PostMapping
    public ResponseEntity<Solicitacao> criar(@RequestBody Solicitacao solicitacao) {
        Solicitacao novaSolicitacao = solicitacaoService.criarSolicitacao(solicitacao);
        return ResponseEntity.status(HttpStatus.CREATED).body(novaSolicitacao);
    }
}
