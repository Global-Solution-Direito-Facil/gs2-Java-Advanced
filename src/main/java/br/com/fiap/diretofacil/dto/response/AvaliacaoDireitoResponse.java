package br.com.fiap.diretofacil.dto.response;

import br.com.fiap.diretofacil.enums.ResultadoElegibilidade;

import java.time.LocalDateTime;

/** Resultado da regra de elegibilidade para um direito, com o motivo. */
public record AvaliacaoDireitoResponse(
        Long id,
        DireitoResumoResponse direito,
        ResultadoElegibilidade resultado,
        String motivo,
        LocalDateTime dataAvaliacao
) {}
