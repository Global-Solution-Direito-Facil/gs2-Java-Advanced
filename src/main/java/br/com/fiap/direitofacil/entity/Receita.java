package br.com.fiap.direitofacil.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Receita enviada pela pessoa para a consulta do Farmácia Popular. A foto fica na API .NET
 * (documento opcional); aqui fica o registro, criado por PKG_FARMACIA_POPULAR.PRC_REGISTRAR_RECEITA.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "T_DF_RECEITA")
public class Receita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_RECEITA")
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_PESSOA", nullable = false)
    private Pessoa pessoa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_DOCUMENTO")
    private Documento documento;

    @CreationTimestamp
    @Column(name = "DT_REGISTRO", nullable = false, updatable = false, columnDefinition = "DATE")
    private LocalDateTime dataRegistro;

    @OneToMany(mappedBy = "receita")
    @OrderBy("id ASC")
    @Builder.Default
    private List<ReceitaItem> itens = new ArrayList<>();
}
