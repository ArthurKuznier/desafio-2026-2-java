package br.edu.unoesc.gestao_documentos.domain;

import br.edu.unoesc.gestao_documentos.audit.AuditoriaListener;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "status")
@EntityListeners(AuditoriaListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Status {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * Deve corresponder a um valor de StatusNome (ABERTA, EM_ANALISE, APROVADA,
     * REPROVADA, EMITIDA)
     */
    @Column(nullable = false, length = 150)
    private String nome;

    /**
     * Codigo do responsavel pela etapa - deve bater com Usuario.codigoResponsavel
     * no RF03
     */
    @Column(nullable = false)
    private int responsavel;

    @Column(nullable = false)
    private boolean finalizaSolicitacao;
}