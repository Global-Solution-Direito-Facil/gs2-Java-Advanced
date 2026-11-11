package br.com.fiap.direitofacil.entity;

import br.com.fiap.direitofacil.config.BooleanToCharacterConverter;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * A regra de elegibilidade em forma de dados: faixa etária, exige PcD ou TEA,
 * fração máxima do salário mínimo per capita e tolerância. 1:1 com o direito.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "T_DF_CRITERIO_DIREITO")
public class CriterioDireito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_CRITERIO")
    private Long id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_DIREITO", nullable = false, unique = true)
    private Direito direito;

    @NotNull
    @Min(0)
    @Builder.Default
    @Column(name = "NUM_IDADE_MIN", nullable = false)
    private Integer idadeMin = 0;

    @Max(130)
    @Column(name = "NUM_IDADE_MAX")
    private Integer idadeMax;

    @NotNull
    @Builder.Default
    @Convert(converter = BooleanToCharacterConverter.class)
    @Column(name = "STA_EXIGE_PCD", nullable = false)
    private Boolean exigePcd = false;

    @NotNull
    @Builder.Default
    @Convert(converter = BooleanToCharacterConverter.class)
    @Column(name = "STA_EXIGE_TEA", nullable = false)
    private Boolean exigeTea = false;

    @DecimalMin("0.0001")
    @Column(name = "FRC_SM_MAX", precision = 6, scale = 4)
    private BigDecimal fracaoSmMax;

    @NotNull
    @DecimalMin("0")
    @DecimalMax("100")
    @Builder.Default
    @Column(name = "PCT_TOLERANCIA", nullable = false, precision = 5, scale = 2)
    private BigDecimal pctTolerancia = BigDecimal.valueOf(20);
}
