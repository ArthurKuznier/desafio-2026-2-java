package br.edu.unoesc.gestao_documentos.domain;

import br.edu.unoesc.gestao_documentos.audit.AuditoriaListener;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "solicitacao")
@EntityListeners(AuditoriaListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Solicitacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull(message = "O aluno é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aluno_id", nullable = false)
    private Aluno aluno;

    @NotNull(message = "O curso é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "curso_id", nullable = false)
    private Curso curso;

    @NotNull(message = "O tipo de documento é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_documento_id", nullable = false)
    private TipoDocumento tipo;

    @Column(nullable = false)
    private LocalDateTime dataSolicitacao;

    @Column(nullable = false)
    private LocalDateTime dataAlteracao;

    private LocalDateTime dataEmissao;

    private String ultimaAtualizacaoPor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id", nullable = false)
    private Status status;

    @NotNull(message = "A prioridade é obrigatória")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Prioridade prioridade;
}