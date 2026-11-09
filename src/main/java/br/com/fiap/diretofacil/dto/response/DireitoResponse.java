package br.com.fiap.diretofacil.dto.response;

import br.com.fiap.diretofacil.enums.GrupoAlvo;
import br.com.fiap.diretofacil.enums.StatusVerificacao;

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
