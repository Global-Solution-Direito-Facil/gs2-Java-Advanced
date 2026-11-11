package br.com.fiap.direitofacil.dto.response;

import br.com.fiap.direitofacil.enums.TipoCondicao;

import java.math.BigDecimal;

/** Regra numérica de um direito. */
public record CriterioDireitoResponse(
        Integer idadeMin,
        Integer idadeMax,
        Boolean exigePcd,
        Boolean exigeTea,
        BigDecimal fracaoSmMax,
        BigDecimal pctTolerancia,
        TipoCondicao tipoExigido,
        Boolean rendaIndividual
) {}
