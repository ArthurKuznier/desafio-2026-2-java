package br.edu.unoesc.gestao_documentos.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import br.edu.unoesc.gestao_documentos.service.StatusService;
import br.edu.unoesc.gestao_documentos.domain.Status;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/status")
public class StatusController {

    private final StatusService statusService;

    public StatusController(StatusService statusService) {
        this.statusService = statusService;
    }

    @PostMapping
    public ResponseEntity<Status> criar(@RequestBody Status status) {
        Status novoStatus = statusService.salvar(status);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoStatus);
    }
}
