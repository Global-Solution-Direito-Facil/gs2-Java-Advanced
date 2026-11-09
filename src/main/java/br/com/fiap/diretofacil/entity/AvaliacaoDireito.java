package br.com.fiap.diretofacil.entity;

import br.com.fiap.diretofacil.enums.ResultadoElegibilidade;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Veredito da regra de elegibilidade para um direito sobre um documento.
 * Guarda todos os resultados, inclusive os negativos com o motivo.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "AVALIACAO_DIREITO",
        uniqueConstraints = @UniqueConstraint(name = "UK_AVALIACAO_DOC_DIREITO", columnNames = {"ID_DOCUMENTO", "ID_DIREITO"}))
public class AvaliacaoDireito {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_AVALIACAO_DIREITO")
    @SequenceGenerator(name = "SEQ_AVALIACAO_DIREITO", sequenceName = "SEQ_AVALIACAO_DIREITO", allocationSize = 1)
    @Column(name = "ID_AVALIACAO")
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_DOCUMENTO", nullable = false)
    private Documento documento;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_DIREITO", nullable = false)
    private Direito direito;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "RESULTADO", nullable = false, length = 20)
    private ResultadoElegibilidade resultado;

    @Size(max = 300)
    @Column(name = "MOTIVO", length = 300)
    private String motivo;

    @CreationTimestamp
    @Column(name = "DATA_AVALIACAO", nullable = false, updatable = false)
    private LocalDateTime dataAvaliacao;
}
