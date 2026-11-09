package br.com.fiap.diretofacil.entity;

import br.com.fiap.diretofacil.config.BooleanToCharacterConverter;
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

/** Perfil do cidadão atendido */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "CIDADAO")
public class Cidadao {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_CIDADAO")
    @SequenceGenerator(name = "SEQ_CIDADAO", sequenceName = "SEQ_CIDADAO", allocationSize = 1)
    @Column(name = "ID_CIDADAO")
    private Long id;

    @NotBlank
    @Size(max = 150)
    @Column(name = "NOME", nullable = false, length = 150)
    private String nome;

    @NotBlank
    @Pattern(regexp = "\\d{11}")
    @Column(name = "CPF", nullable = false, unique = true, length = 11)
    private String cpf;

    @NotNull
    @Past
    @Column(name = "DATA_NASCIMENTO", nullable = false)
    private LocalDate dataNascimento;

    @NotNull
    @PositiveOrZero
    @Column(name = "RENDA_FAMILIAR", nullable = false, precision = 12, scale = 2)
    private BigDecimal rendaFamiliar;

    @NotNull
    @Min(1)
    @Column(name = "QTD_PESSOAS_FAMILIA", nullable = false)
    private Integer qtdPessoasFamilia;

    @NotNull
    @Builder.Default
    @Convert(converter = BooleanToCharacterConverter.class)
    @Column(name = "CADUNICO", nullable = false)
    private Boolean cadunico = false;

    @Size(max = 100)
    @Column(name = "MUNICIPIO", length = 100)
    private String municipio;

    @Size(min = 2, max = 2)
    @Column(name = "UF", length = 2)
    private String uf;

    @CreationTimestamp
    @Column(name = "DATA_CADASTRO", nullable = false, updatable = false)
    private LocalDateTime dataCadastro;

    @OneToMany(mappedBy = "cidadao")
    @Builder.Default
    private List<Documento> documentos = new ArrayList<>();

    @OneToMany(mappedBy = "cidadao")
    @Builder.Default
    private List<Caso> casos = new ArrayList<>();

    public int getIdade() {
        return Period.between(dataNascimento, LocalDate.now()).getYears();
    }

    public BigDecimal getRendaPerCapita() {
        return rendaFamiliar.divide(BigDecimal.valueOf(qtdPessoasFamilia), 2, RoundingMode.HALF_UP);
    }
}
