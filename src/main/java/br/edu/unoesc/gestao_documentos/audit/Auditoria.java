package br.edu.unoesc.gestao_documentos.audit;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "auditoria")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String entidade;

    @Column(name = "entidade_id", nullable = false)
    private String entidadeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoOperacao operacao;

    @Lob
    @Column(name = "estado_anterior")
    private String estadoAnterior;

    @Lob
    @Column(name = "estado_novo")
    private String estadoNovo;

    @Column(nullable = false, length = 150)
    private String usuario;

    @Column(nullable = false)
    private LocalDateTime dataHora;
}