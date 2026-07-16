package br.edu.unoesc.gestao_documentos.controller;

import br.edu.unoesc.gestao_documentos.domain.Status;
import br.edu.unoesc.gestao_documentos.service.StatusService;
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
@RequestMapping("/api/status")
public class StatusController {

    private final StatusService statusService;

    public StatusController(StatusService statusService) {
        this.statusService = statusService;
    }

    @PostMapping
    public ResponseEntity<Status> criar(@RequestBody @Valid Status status) {
        Status novoStatus = statusService.salvar(status);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoStatus);
    }

    @GetMapping
    public ResponseEntity<Page<Status>> listar(Pageable pageable) {
        return ResponseEntity.ok(statusService.listar(pageable));
    }
}