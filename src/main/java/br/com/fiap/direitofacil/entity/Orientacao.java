package br.com.fiap.direitofacil.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Passo a passo em linguagem simples gerado pela IA (RAG) para um caso, com a
 * fonte citada. 1:1 com o caso; gravado via PKG_CASOS.PRC_REGISTRAR_ORIENTACAO.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "T_DF_ORIENTACAO")
public class Orientacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_ORIENTACAO")
    private Long id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_CASO", nullable = false, unique = true)
    private Caso caso;

    @NotBlank
    @Size(max = 4000)
    @Column(name = "DES_PASSO_A_PASSO", nullable = false, length = 4000)
    private String passoAPasso;

    @NotBlank
    @Size(max = 500)
    @Column(name = "DES_FONTE", nullable = false, length = 500)
    private String fonte;

    @Size(max = 100)
    @Column(name = "DES_MODELO", length = 100)
    private String modelo;

    @CreationTimestamp
    @Column(name = "DT_GERACAO", nullable = false, updatable = false, columnDefinition = "DATE")
    private LocalDateTime dataGeracao;
}
