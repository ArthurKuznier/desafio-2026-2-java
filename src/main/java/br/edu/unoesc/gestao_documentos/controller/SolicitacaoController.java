package br.edu.unoesc.gestao_documentos.controller;

import br.edu.unoesc.gestao_documentos.domain.Solicitacao;
import br.edu.unoesc.gestao_documentos.dto.AlterarStatusDto;
import br.edu.unoesc.gestao_documentos.repositories.projection.DocumentoCountProjection;
import br.edu.unoesc.gestao_documentos.repositories.projection.PeriodoCountProjection;
import br.edu.unoesc.gestao_documentos.repositories.projection.StatusCountProjection;
import br.edu.unoesc.gestao_documentos.repositories.projection.TempoMedioEmissaoProjection;
import br.edu.unoesc.gestao_documentos.service.SolicitacaoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/solicitacoes")
public class SolicitacaoController {

    private final SolicitacaoService solicitacaoService;

    public SolicitacaoController(SolicitacaoService solicitacaoService) {
        this.solicitacaoService = solicitacaoService;
    }

    @PostMapping
    public ResponseEntity<Solicitacao> criar(@RequestBody @Valid Solicitacao solicitacao) {
        Solicitacao novaSolicitacao = solicitacaoService.criarSolicitacao(solicitacao);
        return ResponseEntity.status(HttpStatus.CREATED).body(novaSolicitacao);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Solicitacao> alterarStatus(
            @PathVariable Integer id,
            @RequestBody @Valid AlterarStatusDto dto) {
        Solicitacao solicitacaoAtualizada = solicitacaoService.alterarStatus(id, dto.statusId(), dto.responsavel());
        return ResponseEntity.ok(solicitacaoAtualizada);
    }

    // RF02 - filtros: Aluno.nome, Curso.nome, status, periodo, tipoDocumento.nome +
    // paginacao
    @GetMapping
    public ResponseEntity<Page<Solicitacao>> listar(
            @RequestParam(required = false) String nomeAluno,
            @RequestParam(required = false) String nomeCurso,
            @RequestParam(required = false) String tipoDocumento,
            @RequestParam(required = false) Integer statusId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim,
            Pageable pageable) {
        Page<Solicitacao> paginacao = solicitacaoService.buscarSolicitacoes(
                nomeAluno, nomeCurso, tipoDocumento, statusId, dataInicio, dataFim, pageable);
        return ResponseEntity.ok(paginacao);
    }

    // RF02 - "Solicitacoes realizadas por um Aluno"
    @GetMapping("/aluno/{alunoId}")
    public ResponseEntity<Page<Solicitacao>> listarPorAluno(@PathVariable Integer alunoId, Pageable pageable) {
        return ResponseEntity.ok(solicitacaoService.buscarPorAluno(alunoId, pageable));
    }

    // RF02/RF06 - Quantidade de solicitacoes por Status
    @GetMapping("/estatisticas/por-status")
    public ResponseEntity<List<StatusCountProjection>> estatisticasPorStatus() {
        return ResponseEntity.ok(solicitacaoService.estatisticasPorStatus());
    }

    // RF02/RF06 - Quantidade de solicitacoes por periodo
    @GetMapping("/estatisticas/por-periodo")
    public ResponseEntity<List<PeriodoCountProjection>> estatisticasPorPeriodo(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim) {
        return ResponseEntity.ok(solicitacaoService.estatisticasPorPeriodo(dataInicio, dataFim));
    }

    // RF02/RF06 - Documentos mais solicitados
    @GetMapping("/estatisticas/documentos-mais-solicitados")
    public ResponseEntity<List<DocumentoCountProjection>> documentosMaisSolicitados() {
        return ResponseEntity.ok(solicitacaoService.documentosMaisSolicitados());
    }

    // RF02/RF06 - Media de tempo ate emissao
    @GetMapping("/estatisticas/tempo-medio-emissao")
    public ResponseEntity<TempoMedioEmissaoProjection> tempoMedioEmissao() {
        return ResponseEntity.ok(solicitacaoService.tempoMedioEmissao());
    }
}