package br.com.fiap.direitofacil.dto.request;

import br.com.fiap.direitofacil.enums.TipoCondicao;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * Regra numérica de um direito, enviada dentro de DireitoRequest.
 * tipoExigido e rendaIndividual são opcionais (nulos = qualquer deficiência / renda per capita).
 */
public record CriterioDireitoRequest(
        @NotNull @Min(0) Integer idadeMin,
        @Max(130) Integer idadeMax,
        @NotNull Boolean exigePcd,
        @NotNull Boolean exigeTea,
        @DecimalMin("0.0001") BigDecimal fracaoSmMax,
        @NotNull @DecimalMin("0") @DecimalMax("100") BigDecimal pctTolerancia,
        TipoCondicao tipoExigido,
        Boolean rendaIndividual
) {
    /** O CHECK CK_DF_CRITERIO_TIPO aceita só tipos de deficiência; OUTRA não é exigível. */
    @AssertTrue(message = "tipoExigido não pode ser OUTRA")
    public boolean isTipoExigidoValido() {
        return tipoExigido != TipoCondicao.OUTRA;
    }
}
