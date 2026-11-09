package br.com.fiap.diretofacil.entity;

import br.com.fiap.diretofacil.enums.GrupoAlvo;
import br.com.fiap.diretofacil.enums.StatusVerificacao;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

/**
 * Direito do catálogo federal (BPC, CIPTEA, Tarifa Social...), com órgão, base
 * legal e prazo. A regra numérica fica em CriterioDireito.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "T_DF_DIREITO")
public class Direito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_DIREITO")
    private Long id;

    @NotBlank
    @Pattern(regexp = "[A-Z0-9_]{3,40}")
    @Column(name = "COD_DIREITO", nullable = false, unique = true, length = 40)
    private String codigo;

    @NotBlank
    @Size(max = 150)
    @Column(name = "NOME_DIREITO", nullable = false, length = 150)
    private String nome;

    @NotBlank
    @Size(max = 100)
    @Column(name = "DES_ORGAO", nullable = false, length = 100)
    private String orgao;

    @NotBlank
    @Size(max = 200)
    @Column(name = "DES_BASE_LEGAL", nullable = false, length = 200)
    private String baseLegal;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "GRUPO_ALVO", nullable = false, length = 20)
    private GrupoAlvo grupoAlvo;

    @NotNull
    @Positive
    @Builder.Default
    @Column(name = "NUM_PRAZO_DIAS", nullable = false)
    private Integer prazoDias = 30;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "STA_VERIFICACAO", nullable = false, length = 10)
    private StatusVerificacao statusVerificacao = StatusVerificacao.CONFIRMAR;

    @OneToOne(mappedBy = "direito", fetch = FetchType.LAZY)
    private CriterioDireito criterio;
}
