package br.com.fiap.direitofacil.entity;

import br.com.fiap.direitofacil.enums.ResultadoElegibilidade;
import br.com.fiap.direitofacil.enums.StatusCaso;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Pedido de um direito para uma pessoa, aberto pela PKG_ELEGIBILIDADE quando o
 * resultado é ELEGIVEL ou POSSIVEL. Status muda só via PKG_CASOS, que respeita
 * o fluxo validado pela trigger e gera a auditoria.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "T_DF_CASO")
public class Caso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_CASO")
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_PESSOA", nullable = false)
    private Pessoa pessoa;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_DIREITO", nullable = false)
    private Direito direito;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "STATUS", nullable = false, length = 25)
    private StatusCaso status = StatusCaso.ABERTO;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "RESULTADO_AVALIACAO", nullable = false, length = 15)
    private ResultadoElegibilidade resultadoAvaliacao;

    @NotBlank
    @Size(max = 300)
    @Column(name = "DES_MOTIVO", nullable = false, length = 300)
    private String motivo;

    @NotNull
    @Column(name = "DT_ABERTURA", nullable = false, columnDefinition = "DATE")
    private LocalDateTime dataAbertura;

    @NotNull
    @Column(name = "DT_PRAZO", nullable = false, columnDefinition = "DATE")
    private LocalDateTime dataPrazo;

    @NotNull
    @Column(name = "DT_ATUALIZACAO", nullable = false, columnDefinition = "DATE")
    private LocalDateTime dataAtualizacao;

    @OneToOne(mappedBy = "caso", fetch = FetchType.LAZY)
    private Orientacao orientacao;
}
