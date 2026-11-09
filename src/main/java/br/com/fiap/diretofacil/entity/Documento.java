package br.com.fiap.diretofacil.entity;

import br.com.fiap.diretofacil.enums.StatusDocumento;
import br.com.fiap.diretofacil.enums.TipoDocumento;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Laudo enviado pelo cidadão, pelo .NET ou pelo upload direto, e o resultado
 * da extração feita pela IA (condição, CID e idade).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "DOCUMENTO")
public class Documento {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_DOCUMENTO")
    @SequenceGenerator(name = "SEQ_DOCUMENTO", sequenceName = "SEQ_DOCUMENTO", allocationSize = 1)
    @Column(name = "ID_DOCUMENTO")
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_CIDADAO", nullable = false)
    private Cidadao cidadao;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "TIPO", nullable = false, length = 30)
    private TipoDocumento tipo = TipoDocumento.LAUDO_MEDICO;

    @NotBlank
    @Size(max = 255)
    @Column(name = "NOME_ARQUIVO", nullable = false, length = 255)
    private String nomeArquivo;

    @Size(max = 100)
    @Column(name = "CONTENT_TYPE", length = 100)
    private String contentType;

    @Size(max = 1000)
    @Column(name = "URL_ARQUIVO", length = 1000)
    private String urlArquivo;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "CONTEUDO")
    private byte[] conteudo;

    @NotBlank
    @Size(max = 100)
    @Column(name = "IDEMPOTENCY_KEY", nullable = false, unique = true, length = 100)
    private String idempotencyKey;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "STATUS", nullable = false, length = 20)
    private StatusDocumento status = StatusDocumento.RECEBIDO;

    @Size(max = 200)
    @Column(name = "CONDICAO", length = 200)
    private String condicao;

    @Size(max = 20)
    @Column(name = "CID", length = 20)
    private String cid;

    @Min(0)
    @Max(130)
    @Column(name = "IDADE_EXTRAIDA")
    private Integer idadeExtraida;

    @DecimalMin("0.00")
    @DecimalMax("1.00")
    @Column(name = "CONFIANCA_EXTRACAO", precision = 3, scale = 2)
    private BigDecimal confiancaExtracao;

    @Size(max = 50)
    @Column(name = "MONGO_ID", length = 50)
    private String mongoId;

    @Size(max = 500)
    @Column(name = "MENSAGEM_ERRO", length = 500)
    private String mensagemErro;

    @CreationTimestamp
    @Column(name = "DATA_ENVIO", nullable = false, updatable = false)
    private LocalDateTime dataEnvio;

    @Column(name = "DATA_PROCESSAMENTO")
    private LocalDateTime dataProcessamento;

    @OneToMany(mappedBy = "documento")
    @Builder.Default
    private List<AvaliacaoDireito> avaliacoes = new ArrayList<>();

    public void descartarConteudo() {
        this.conteudo = null;
    }
}
