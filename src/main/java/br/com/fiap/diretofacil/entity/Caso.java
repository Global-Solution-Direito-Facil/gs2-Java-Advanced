package br.com.fiap.diretofacil.entity;

import br.com.fiap.diretofacil.enums.StatusCaso;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Acompanhamento de um pedido: um direito apontado como ELEGIVEL ou POSSIVEL
 * para um cidadão, com o passo a passo gerado pela IA e o status que o gestor movimenta.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "CASO")
public class Caso {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_CASO")
    @SequenceGenerator(name = "SEQ_CASO", sequenceName = "SEQ_CASO", allocationSize = 1)
    @Column(name = "ID_CASO")
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_CIDADAO", nullable = false)
    private Cidadao cidadao;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_DIREITO", nullable = false)
    private Direito direito;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_AVALIACAO", nullable = false, unique = true)
    private AvaliacaoDireito avaliacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_GESTOR_RESPONSAVEL")
    private Gestor gestorResponsavel;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "STATUS", nullable = false, length = 20)
    private StatusCaso status = StatusCaso.ABERTO;

    @Column(name = "PRAZO")
    private LocalDate prazo;

    @Lob
    @Column(name = "PASSO_A_PASSO")
    private String passoAPasso;

    @Size(max = 1000)
    @Column(name = "FONTES", length = 1000)
    private String fontes;

    @Size(max = 1000)
    @Column(name = "OBSERVACAO_GESTOR", length = 1000)
    private String observacaoGestor;

    @CreationTimestamp
    @Column(name = "DATA_ABERTURA", nullable = false, updatable = false)
    private LocalDateTime dataAbertura;

    @UpdateTimestamp
    @Column(name = "DATA_ATUALIZACAO", nullable = false)
    private LocalDateTime dataAtualizacao;
}
