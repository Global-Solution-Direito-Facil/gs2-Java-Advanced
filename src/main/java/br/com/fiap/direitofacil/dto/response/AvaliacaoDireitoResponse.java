package br.com.fiap.direitofacil.dto.response;

import br.com.fiap.direitofacil.enums.ResultadoElegibilidade;

/** Veredito da regra PL/SQL para um direito, calculado na hora e com o motivo. */
public record AvaliacaoDireitoResponse(
        DireitoResumoResponse direito,
        ResultadoElegibilidade resultado,
        String motivo
) {}
