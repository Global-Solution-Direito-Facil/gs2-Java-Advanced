package br.com.fiap.diretofacil.entity;

import br.com.fiap.diretofacil.config.BooleanToCharacterConverter;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/** Perfil do gestor (assistente social) que acompanha os casos. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "GESTOR")
public class Gestor {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_GESTOR")
    @SequenceGenerator(name = "SEQ_GESTOR", sequenceName = "SEQ_GESTOR", allocationSize = 1)
    @Column(name = "ID_GESTOR")
    private Long id;

    @NotBlank
    @Size(max = 150)
    @Column(name = "NOME", nullable = false, length = 150)
    private String nome;

    @NotBlank
    @Pattern(regexp = "\\d{11}")
    @Column(name = "CPF", nullable = false, unique = true, length = 11)
    private String cpf;

    @NotBlank
    @Size(max = 30)
    @Column(name = "MATRICULA", nullable = false, unique = true, length = 30)
    private String matricula;

    @NotBlank
    @Size(max = 150)
    @Column(name = "UNIDADE", nullable = false, length = 150)
    private String unidade;

    @Size(max = 20)
    @Column(name = "TELEFONE", length = 20)
    private String telefone;

    @NotNull
    @Builder.Default
    @Convert(converter = BooleanToCharacterConverter.class)
    @Column(name = "ATIVO", nullable = false)
    private Boolean ativo = true;

    @CreationTimestamp
    @Column(name = "DATA_CADASTRO", nullable = false, updatable = false)
    private LocalDateTime dataCadastro;
}
