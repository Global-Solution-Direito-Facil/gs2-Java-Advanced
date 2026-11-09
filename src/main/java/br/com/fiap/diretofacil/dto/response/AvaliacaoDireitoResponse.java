package br.com.fiap.diretofacil.dto.response;

import br.com.fiap.diretofacil.enums.ResultadoElegibilidade;

/** Veredito da regra PL/SQL para um direito, calculado na hora e com o motivo. */
public record AvaliacaoDireitoResponse(
        DireitoResumoResponse direito,
        ResultadoElegibilidade resultado,
        String motivo
) {}
