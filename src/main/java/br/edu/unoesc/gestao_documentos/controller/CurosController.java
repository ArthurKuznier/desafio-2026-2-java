package br.edu.unoesc.gestao_documentos.controller;

import org.springframework.web.bind.annotation.*;
import br.edu.unoesc.gestao_documentos.service.CursoService;
import br.edu.unoesc.gestao_documentos.domain.Curso;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/cursos")
public class CurosController {

    private final CursoService cursoService;

    public CurosController(CursoService cursoService) {
        this.cursoService = cursoService;
    }

    @PostMapping
    public ResponseEntity<Curso> criar(@RequestBody Curso curso) {
        Curso novoCurso = cursoService.salvar(curso);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoCurso);
    }
}
