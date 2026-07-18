package br.edu.unoesc.gestao_documentos.domain;

import br.edu.unoesc.gestao_documentos.audit.AuditoriaListener;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "aluno")
@EntityListeners(AuditoriaListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Aluno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "O nome do aluno é obrigatório")
    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false)
    private boolean ativo = true;

    @JsonIgnore
    @OneToMany(mappedBy = "aluno")
    private Set<Solicitacao> solicitacoes = new HashSet<>();
}