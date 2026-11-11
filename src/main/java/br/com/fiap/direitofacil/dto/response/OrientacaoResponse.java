package br.com.fiap.direitofacil.dto.response;

import java.time.LocalDateTime;

/** Passo a passo gerado pela IA com a fonte citada. */
public record OrientacaoResponse(
        Long id,
        String passoAPasso,
        String fonte,
        String modelo,
        LocalDateTime dataGeracao
) {}
