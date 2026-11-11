package br.com.fiap.direitofacil.entity;

import br.com.fiap.direitofacil.enums.Role;
import br.com.fiap.direitofacil.validation.Cpf;
import br.com.fiap.direitofacil.validation.EmailValido;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Conta de login do app, com os dados pessoais de quem loga. O cidadão é o
 * responsável que cadastra os beneficiários (Pessoa); o gestor não tem pessoas.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "T_DF_USUARIO")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_USUARIO")
    private Long id;

    @NotBlank
    @Size(max = 150)
    @Column(name = "NOME", nullable = false, length = 150)
    private String nome;

    @NotBlank
    @Cpf
    @Size(max = 14)
    @Column(name = "CPF", nullable = false, unique = true, length = 14)
    private String cpf;

    @NotBlank
    @EmailValido
    @Size(max = 150)
    @Column(name = "EMAIL", nullable = false, unique = true, length = 150)
    private String email;

    @NotBlank
    @Size(max = 255)
    @Column(name = "SENHA", nullable = false, length = 255)
    private String senha;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "PERFIL", nullable = false, length = 10)
    private Role perfil;

    @CreationTimestamp
    @Column(name = "DT_CADASTRO", nullable = false, updatable = false, columnDefinition = "DATE")
    private LocalDateTime dataCadastro;

    @OneToMany(mappedBy = "usuario")
    @Builder.Default
    private List<Pessoa> pessoas = new ArrayList<>();
}
