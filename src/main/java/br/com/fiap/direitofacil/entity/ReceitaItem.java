package br.com.fiap.direitofacil.entity;

import br.com.fiap.direitofacil.config.BooleanToCharacterConverter;
import br.com.fiap.direitofacil.enums.ResultadoFarmaciaPopular;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Remédio da receita já conferido pela pessoa: o que a IA leu, o que foi confirmado,
 * se houve correção e o resultado da consulta ao elenco. Gravado por
 * PKG_FARMACIA_POPULAR.PRC_ADICIONAR_ITEM, que decide o resultado e a orientação.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "T_DF_RECEITA_ITEM")
public class ReceitaItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_ITEM")
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_RECEITA", nullable = false)
    private Receita receita;

    @NotBlank
    @Size(max = 150)
    @Column(name = "DES_NOME_LIDO", nullable = false, length = 150)
    private String nomeLido;

    @NotBlank
    @Size(max = 150)
    @Column(name = "DES_PRINCIPIO_ATIVO", nullable = false, length = 150)
    private String principioAtivo;

    @Size(max = 60)
    @Column(name = "DES_CONCENTRACAO", length = 60)
    private String concentracao;

    @NotNull
    @Builder.Default
    @Convert(converter = BooleanToCharacterConverter.class)
    @Column(name = "STA_CORRIGIDO", nullable = false)
    private Boolean corrigido = false;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "RESULTADO", nullable = false, length = 20)
    private ResultadoFarmaciaPopular resultado;

    /** Item do elenco encontrado; nulo quando NAO_ENCONTRADO. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_MEDICAMENTO")
    private MedicamentoFarmaciaPopular medicamento;

    @NotBlank
    @Size(max = 400)
    @Column(name = "DES_ORIENTACAO", nullable = false, length = 400)
    private String orientacao;
}
