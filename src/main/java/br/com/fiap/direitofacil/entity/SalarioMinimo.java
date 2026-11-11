package br.com.fiap.direitofacil.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

/** Valor do salário mínimo por ano, referência dos critérios de renda. Só leitura pela API. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "T_DF_SALARIO_MINIMO")
public class SalarioMinimo {

    @Id
    @Column(name = "ANO_REFERENCIA")
    private Integer anoReferencia;

    @NotNull
    @Positive
    @Column(name = "VLR_SALARIO_MINIMO", nullable = false, precision = 8, scale = 2)
    private BigDecimal valor;

    @Size(max = 200)
    @Column(name = "DES_FONTE", length = 200)
    private String fonte;
}
