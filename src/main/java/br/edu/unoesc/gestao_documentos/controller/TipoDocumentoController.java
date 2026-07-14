package br.edu.unoesc.gestao_documentos.controller;

import br.edu.unoesc.gestao_documentos.service.TipoDocumentoService;
import br.edu.unoesc.gestao_documentos.domain.TipoDocumento;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/tipoDocumento")
public class TipoDocumentoController {

    private final TipoDocumentoService tipoDocumentoService;

    public TipoDocumentoController(TipoDocumentoService tipoDocumentoService) {
        this.tipoDocumentoService = tipoDocumentoService;
    }

    @PostMapping
    public ResponseEntity<TipoDocumento> criar(@RequestBody TipoDocumento tipoDocumento) {
        TipoDocumento novoTipoDocumento = tipoDocumentoService.salvar(tipoDocumento);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoTipoDocumento);
    }
}
