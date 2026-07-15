package br.edu.unoesc.gestao_documentos.domain;

import br.edu.unoesc.gestao_documentos.audit.AuditoriaListener;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tipo_documento")
@EntityListeners(AuditoriaListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class TipoDocumento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 150)
    private String nome;
}