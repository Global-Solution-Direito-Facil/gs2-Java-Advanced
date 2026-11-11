package br.com.fiap.direitofacil.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/** Regra numérica de um direito, enviada dentro de DireitoRequest. */
public record CriterioDireitoRequest(
        @NotNull @Min(0) Integer idadeMin,
        @Max(130) Integer idadeMax,
        @NotNull Boolean exigePcd,
        @NotNull Boolean exigeTea,
        @DecimalMin("0.0001") BigDecimal fracaoSmMax,
        @NotNull @DecimalMin("0") @DecimalMax("100") BigDecimal pctTolerancia
) {}
