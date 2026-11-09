package br.com.fiap.diretofacil.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

/**
 * Beneficiário acompanhado por um usuário responsável (ex.: o filho autista da
 * Ana). Idade, renda familiar e número de membros são o que a regra PL/SQL lê.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "T_DF_PESSOA")
public class Pessoa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_PESSOA")
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_USUARIO", nullable = false)
    private Usuario usuario;

    @NotBlank
    @Size(max = 150)
    @Column(name = "NOME", nullable = false, length = 150)
    private String nome;

    @NotNull
    @Past
    @Column(name = "DT_NASCIMENTO", nullable = false)
    private LocalDate dataNascimento;

    @NotNull
    @PositiveOrZero
    @Column(name = "VLR_RENDA_FAMILIAR", nullable = false, precision = 10, scale = 2)
    private BigDecimal rendaFamiliar;

    @NotNull
    @Min(1)
    @Column(name = "QTD_MEMBROS_FAMILIA", nullable = false)
    private Integer qtdMembrosFamilia;

    @CreationTimestamp
    @Column(name = "DT_CADASTRO", nullable = false, updatable = false, columnDefinition = "DATE")
    private LocalDateTime dataCadastro;

    @OneToMany(mappedBy = "pessoa")
    @Builder.Default
    private List<Laudo> laudos = new ArrayList<>();

    @OneToMany(mappedBy = "pessoa")
    @Builder.Default
    private List<Caso> casos = new ArrayList<>();

    public int getIdade() {
        return Period.between(dataNascimento, LocalDate.now()).getYears();
    }

    public BigDecimal getRendaPerCapita() {
        return rendaFamiliar.divide(BigDecimal.valueOf(qtdMembrosFamilia), 2, RoundingMode.HALF_UP);
    }
}
