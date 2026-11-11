package br.com.fiap.direitofacil.dto.response;

import br.com.fiap.direitofacil.enums.GrupoAlvo;
import br.com.fiap.direitofacil.enums.StatusVerificacao;

/** Direito do catálogo com o critério aninhado. */
public record DireitoResponse(
        Long id,
        String codigo,
        String nome,
        String orgao,
        String baseLegal,
        GrupoAlvo grupoAlvo,
        Integer prazoDias,
        StatusVerificacao statusVerificacao,
        CriterioDireitoResponse criterio
) {}
