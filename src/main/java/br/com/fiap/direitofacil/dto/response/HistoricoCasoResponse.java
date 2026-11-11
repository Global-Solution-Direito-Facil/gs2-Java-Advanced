package br.com.fiap.direitofacil.dto.response;

import br.com.fiap.direitofacil.enums.TipoOperacaoAuditoria;

import java.time.LocalDateTime;

/** Linha do histórico do caso, lida da trilha de auditoria gravada pelo gatilho do banco. */
public record HistoricoCasoResponse(
        Long id,
        TipoOperacaoAuditoria operacao,
        LocalDateTime data,
        String statusAnterior,
        String statusNovo
) {}
