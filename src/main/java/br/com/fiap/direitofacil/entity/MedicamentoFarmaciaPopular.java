package br.com.fiap.direitofacil.entity;

import br.com.fiap.direitofacil.config.BooleanToCharacterConverter;
import br.com.fiap.direitofacil.enums.IndicacaoFarmaciaPopular;
import br.com.fiap.direitofacil.enums.TipoItemFarmaciaPopular;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

/**
 * Item do elenco oficial do Programa Farmácia Popular (39 medicamentos e 2 insumos,
 * carregados pelo Flyway). É a tabela, e não a IA, que decide se o remédio é gratuito.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "T_DF_MEDICAMENTO_FP")
public class MedicamentoFarmaciaPopular {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_MEDICAMENTO")
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "COD_INDICACAO", nullable = false, length = 30)
    private IndicacaoFarmaciaPopular indicacao;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "TIPO_ITEM", nullable = false, length = 12)
    private TipoItemFarmaciaPopular tipoItem = TipoItemFarmaciaPopular.MEDICAMENTO;

    @NotBlank
    @Size(max = 150)
    @Column(name = "DES_PRINCIPIO_ATIVO", nullable = false, length = 150)
    private String principioAtivo;

    /** Nula nos insumos. */
    @Size(max = 60)
    @Column(name = "DES_CONCENTRACAO", length = 60)
    private String concentracao;

    @Size(max = 100)
    @Column(name = "DES_OBSERVACAO", length = 100)
    private String observacao;

    @NotNull
    @Builder.Default
    @Convert(converter = BooleanToCharacterConverter.class)
    @Column(name = "STA_GRATUITO", nullable = false)
    private Boolean gratuito = true;

    @NotBlank
    @Size(max = 300)
    @Column(name = "DES_FONTE", nullable = false, length = 300)
    private String fonte;

    @NotNull
    @Column(name = "DT_ELENCO", nullable = false)
    private LocalDate dataElenco;
}
