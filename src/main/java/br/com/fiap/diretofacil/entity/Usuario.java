package br.com.fiap.diretofacil.entity;

import br.com.fiap.diretofacil.config.BooleanToCharacterConverter;
import br.com.fiap.diretofacil.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/** Conta de login; aponta para o perfil (Cidadao ou Gestor) conforme a role. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "USUARIO")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_USUARIO")
    @SequenceGenerator(name = "SEQ_USUARIO", sequenceName = "SEQ_USUARIO", allocationSize = 1)
    @Column(name = "ID_USUARIO")
    private Long id;

    @NotBlank
    @Email
    @Size(max = 150)
    @Column(name = "EMAIL", nullable = false, unique = true, length = 150)
    private String email;

    @NotBlank
    @Size(max = 100)
    @Column(name = "SENHA_HASH", nullable = false, length = 100)
    private String senhaHash;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "ROLE", nullable = false, length = 20)
    private Role role;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_CIDADAO", unique = true)
    private Cidadao cidadao;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_GESTOR", unique = true)
    private Gestor gestor;

    @NotNull
    @Builder.Default
    @Convert(converter = BooleanToCharacterConverter.class)
    @Column(name = "ATIVO", nullable = false)
    private Boolean ativo = true;

    @CreationTimestamp
    @Column(name = "DATA_CADASTRO", nullable = false, updatable = false)
    private LocalDateTime dataCadastro;

    public Long getPerfilId() {
        if (role == Role.CIDADAO && cidadao != null) {
            return cidadao.getId();
        }
        if (role == Role.GESTOR && gestor != null) {
            return gestor.getId();
        }
        return null;
    }
}
