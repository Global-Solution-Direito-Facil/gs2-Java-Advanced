package br.com.fiap.direitofacil.entity;

import br.com.fiap.direitofacil.enums.StatusDocumento;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Registro, neste banco, de cada laudo anunciado pelo evento DocumentoRecebido.
 * O arquivo fica na API .NET (idExterno); aqui só metadados e a chave de idempotência.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "T_DF_DOCUMENTO")
public class Documento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_DOCUMENTO")
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_PESSOA", nullable = false)
    private Pessoa pessoa;

    @NotBlank
    @Size(max = 100)
    @Column(name = "DES_ID_EXTERNO", nullable = false, unique = true, length = 100)
    private String idExterno;

    @NotBlank
    @Size(max = 200)
    @Column(name = "DES_NOME_ARQUIVO", nullable = false, length = 200)
    private String nomeArquivo;

    @NotBlank
    @Size(max = 50)
    @Column(name = "DES_TIPO_MIDIA", nullable = false, length = 50)
    private String tipoMidia;

    @NotNull
    @Positive
    @Column(name = "NUM_TAMANHO_BYTES", nullable = false)
    private Long tamanhoBytes;

    @NotBlank
    @Size(max = 100)
    @Column(name = "DES_CHAVE_IDEMPOTENCIA", nullable = false, unique = true, length = 100)
    private String chaveIdempotencia;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "STATUS", nullable = false, length = 15)
    private StatusDocumento status = StatusDocumento.RECEBIDO;

    @CreationTimestamp
    @Column(name = "DT_RECEBIMENTO", nullable = false, updatable = false, columnDefinition = "DATE")
    private LocalDateTime dataRecebimento;

    @Column(name = "DT_PROCESSAMENTO", columnDefinition = "DATE")
    private LocalDateTime dataProcessamento;
}
