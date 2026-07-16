package br.edu.unoesc.gestao_documentos.controller;

import br.edu.unoesc.gestao_documentos.domain.Curso;
import br.edu.unoesc.gestao_documentos.service.CursoService;
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
@RequestMapping("/api/cursos")
public class CursoController {

    private final CursoService cursoService;

    public CursoController(CursoService cursoService) {
        this.cursoService = cursoService;
    }

    @PostMapping
    public ResponseEntity<Curso> criar(@RequestBody @Valid Curso curso) {
        Curso novoCurso = cursoService.salvar(curso);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoCurso);
    }

    @GetMapping
    public ResponseEntity<Page<Curso>> listar(Pageable pageable) {
        return ResponseEntity.ok(cursoService.listar(pageable));
    }
}