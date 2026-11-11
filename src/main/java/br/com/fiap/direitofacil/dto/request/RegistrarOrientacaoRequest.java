package br.com.fiap.direitofacil.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.nio.charset.StandardCharsets;

/** Passo a passo e fonte de um caso, gravados ou substituídos em PUT /casos/{id}/orientacao. */
public record RegistrarOrientacaoRequest(
        @NotBlank @Size(max = 4000) String passoAPasso,
        @NotBlank @Size(max = 500) String fonte,
        @Size(max = 100) String modelo
) {
    /** A coluna DES_PASSO_A_PASSO é VARCHAR2(4000) em bytes; acentos ocupam 2 bytes em UTF-8. */
    @AssertTrue(message = "passoAPasso excede 4000 bytes em UTF-8")
    public boolean isPassoAPassoDentroDoLimiteDeBytes() {
        return passoAPasso == null || passoAPasso.getBytes(StandardCharsets.UTF_8).length <= 4000;
    }

    /** A coluna DES_FONTE é VARCHAR2(500) em bytes. */
    @AssertTrue(message = "fonte excede 500 bytes em UTF-8")
    public boolean isFonteDentroDoLimiteDeBytes() {
        return fonte == null || fonte.getBytes(StandardCharsets.UTF_8).length <= 500;
    }
}
