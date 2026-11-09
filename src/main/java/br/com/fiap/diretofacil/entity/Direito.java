package br.com.fiap.diretofacil.entity;

import br.com.fiap.diretofacil.config.BooleanToCharacterConverter;
import br.com.fiap.diretofacil.enums.Publico;
import br.com.fiap.diretofacil.enums.StatusConferencia;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

/**
 * Direito do catálogo (BPC, Tarifa Social, CIPTEA...), com os textos para o cidadão
 * e os critérios numéricos que a regra de elegibilidade compara.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "DIREITO")
public class Direito {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_DIREITO")
    @SequenceGenerator(name = "SEQ_DIREITO", sequenceName = "SEQ_DIREITO", allocationSize = 1)
    @Column(name = "ID_DIREITO")
    private Long id;

    @NotBlank
    @Pattern(regexp = "[A-Z0-9_]{3,50}")
    @Column(name = "CODIGO", nullable = false, unique = true, length = 50)
    private String codigo;

    @NotBlank
    @Size(max = 150)
    @Column(name = "NOME", nullable = false, length = 150)
    private String nome;

    @NotBlank
    @Size(max = 500)
    @Column(name = "CRITERIOS", nullable = false, length = 500)
    private String criterios;

    @NotBlank
    @Size(max = 200)
    @Column(name = "ONDE_PEDIR", nullable = false, length = 200)
    private String ondePedir;

    @NotBlank
    @Size(max = 200)
    @Column(name = "BASE_LEGAL", nullable = false, length = 200)
    private String baseLegal;   

    @Size(max = 500)
    @Column(name = "URL_BASE_LEGAL", length = 500)
    private String urlBaseLegal;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "DIREITO_PUBLICO", joinColumns = @JoinColumn(name = "ID_DIREITO"))
    @Enumerated(EnumType.STRING)
    @Column(name = "PUBLICO", nullable = false, length = 20)
    @Builder.Default
    private Set<Publico> publicos = new HashSet<>();

    @DecimalMin("0.01")
    @Column(name = "RENDA_PER_CAPITA_MAX_SM", precision = 5, scale = 2)
    private BigDecimal rendaPerCapitaMaxSm;

    @Min(0)
    @Max(120)
    @Column(name = "IDADE_MINIMA")
    private Integer idadeMinima;

    @NotNull
    @Builder.Default
    @Convert(converter = BooleanToCharacterConverter.class)
    @Column(name = "EXIGE_CADUNICO", nullable = false)
    private Boolean exigeCadunico = false;

    @NotNull
    @Builder.Default
    @Convert(converter = BooleanToCharacterConverter.class)
    @Column(name = "EXIGE_DEFICIENCIA", nullable = false)
    private Boolean exigeDeficiencia = false;

    @NotNull
    @Builder.Default
    @Convert(converter = BooleanToCharacterConverter.class)
    @Column(name = "EXIGE_TEA", nullable = false)
    private Boolean exigeTea = false;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "STATUS_CONFERENCIA", nullable = false, length = 20)
    private StatusConferencia statusConferencia = StatusConferencia.CONFIRMAR;

    @NotNull
    @Builder.Default
    @Convert(converter = BooleanToCharacterConverter.class)
    @Column(name = "ATIVO", nullable = false)
    private Boolean ativo = true;
}
