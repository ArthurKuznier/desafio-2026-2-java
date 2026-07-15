package br.edu.unoesc.gestao_documentos.controller;

import br.edu.unoesc.gestao_documentos.service.SolicitacaoService;
import br.edu.unoesc.gestao_documentos.domain.Solicitacao;
import br.edu.unoesc.gestao_documentos.dto.AlterarStatusDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @PatchMapping("/{id}")
    public ResponseEntity<Solicitacao> alterarStatus(
            @PathVariable Integer id,
            @RequestBody AlterarStatusDto dto) {
        Solicitacao solicitacaoAtualizada = solicitacaoService.alterarStatus(id, dto.statusId(), dto.responsavel());

        return ResponseEntity.ok(solicitacaoAtualizada);
    }

    @GetMapping
    public ResponseEntity<Page<Solicitacao>> listar(
            @RequestParam(required = false) String nomeAluno,
            @RequestParam(required = false) Integer cursoId,
            Pageable pageable) {
        Page<Solicitacao> paginacao = solicitacaoService.buscarSolicitacoes(nomeAluno, cursoId, pageable);
        return ResponseEntity.ok(paginacao);
    }
}
