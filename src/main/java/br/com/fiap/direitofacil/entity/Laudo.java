package br.com.fiap.direitofacil.entity;

import br.com.fiap.direitofacil.enums.TipoCondicao;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Dados estruturados extraídos do laudo pela IA (CID, condição e tipo). O texto
 * bruto fica no MongoDB; é o tipo da condição que a regra PL/SQL consulta.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "T_DF_LAUDO")
public class Laudo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_LAUDO")
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_PESSOA", nullable = false)
    private Pessoa pessoa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_DOCUMENTO")
    private Documento documento;

    @NotBlank
    @Size(max = 10)
    @Column(name = "COD_CID", nullable = false, length = 10)
    private String codCid;

    @NotBlank
    @Size(max = 200)
    @Column(name = "DES_CONDICAO", nullable = false, length = 200)
    private String condicao;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "TIPO_CONDICAO", nullable = false, length = 20)
    private TipoCondicao tipoCondicao;

    @NotNull
    @Column(name = "DT_LAUDO", nullable = false)
    private LocalDate dataLaudo;

    @CreationTimestamp
    @Column(name = "DT_CADASTRO", nullable = false, updatable = false, columnDefinition = "DATE")
    private LocalDateTime dataCadastro;
}
