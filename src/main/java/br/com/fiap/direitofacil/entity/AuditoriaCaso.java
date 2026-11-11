package br.com.fiap.direitofacil.entity;

import br.com.fiap.direitofacil.enums.TipoOperacaoAuditoria;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Immutable;

import java.time.LocalDateTime;

/**
 * Linha da trilha de auditoria gravada pelo gatilho TRG_DF_AUDITORIA_CASO (via PKG_AUDITORIA).
 * Só leitura: o Java nunca grava aqui. ID_CASO_REF não é FK porque a trilha guarda também
 * os casos removidos na reavaliação.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Immutable
@Entity
@Table(name = "T_DF_AUDITORIA_CASO")
public class AuditoriaCaso {

    @Id
    @Column(name = "ID_AUDITORIA")
    private Long id;

    @Column(name = "NOME_USUARIO", nullable = false, length = 100)
    private String nomeUsuario;

    @Enumerated(EnumType.STRING)
    @Column(name = "TIPO_OPERACAO", nullable = false, length = 10)
    private TipoOperacaoAuditoria tipoOperacao;

    @Column(name = "DATA_OPERACAO", nullable = false, columnDefinition = "DATE")
    private LocalDateTime dataOperacao;

    @Column(name = "ID_CASO_REF")
    private Long casoId;

    @Column(name = "STATUS_ANTERIOR", length = 25)
    private String statusAnterior;

    @Column(name = "STATUS_NOVO", length = 25)
    private String statusNovo;

    @Column(name = "VALORES_ANTERIORES", length = 1000)
    private String valoresAnteriores;

    @Column(name = "VALORES_NOVOS", length = 1000)
    private String valoresNovos;
}
