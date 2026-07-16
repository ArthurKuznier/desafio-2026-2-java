package br.edu.unoesc.gestao_documentos.controller;

import br.edu.unoesc.gestao_documentos.domain.TipoDocumento;
import br.edu.unoesc.gestao_documentos.service.TipoDocumentoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tipoDocumento")
public class TipoDocumentoController {

    private final TipoDocumentoService tipoDocumentoService;

    public TipoDocumentoController(TipoDocumentoService tipoDocumentoService) {
        this.tipoDocumentoService = tipoDocumentoService;
    }

    @PostMapping
    public ResponseEntity<TipoDocumento> criar(@RequestBody @Valid TipoDocumento tipoDocumento) {
        TipoDocumento novoTipoDocumento = tipoDocumentoService.salvar(tipoDocumento);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoTipoDocumento);
    }

    @GetMapping
    public ResponseEntity<Page<TipoDocumento>> listar(Pageable pageable) {
        return ResponseEntity.ok(tipoDocumentoService.listar(pageable));
    }
}