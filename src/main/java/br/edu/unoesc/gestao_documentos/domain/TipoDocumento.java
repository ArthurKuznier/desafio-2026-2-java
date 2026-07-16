package br.edu.unoesc.gestao_documentos.domain;

import br.edu.unoesc.gestao_documentos.audit.AuditoriaListener;
import jakarta.persistence.*;
import lombok.*;

import jakarta.validation.constraints.NotBlank;

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

    @NotBlank(message = "O nome do tipo de documento é obrigatório")
    @Column(nullable = false, length = 150)
    private String nome;
}