package br.com.fiap.direitofacil.dto.response;

import java.math.BigDecimal;

/** Regra numérica de um direito. */
public record CriterioDireitoResponse(
        Integer idadeMin,
        Integer idadeMax,
        Boolean exigePcd,
        Boolean exigeTea,
        BigDecimal fracaoSmMax,
        BigDecimal pctTolerancia
) {}
