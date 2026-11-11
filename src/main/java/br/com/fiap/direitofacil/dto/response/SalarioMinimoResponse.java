package br.com.fiap.direitofacil.dto.response;

import java.math.BigDecimal;

/** Salário mínimo de referência usado nos critérios de renda. */
public record SalarioMinimoResponse(
        Integer anoReferencia,
        BigDecimal valor,
        String fonte
) {}
