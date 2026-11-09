package br.com.fiap.diretofacil.dto.response;

import br.com.fiap.diretofacil.enums.ResultadoElegibilidade;
import br.com.fiap.diretofacil.enums.StatusCaso;

import java.time.LocalDateTime;

/** Caso com pessoa, direito, resultado da regra e a orientação quando existir. */
public record CasoResponse(
        Long id,
        PessoaResumoResponse pessoa,
        DireitoResumoResponse direito,
        StatusCaso status,
        ResultadoElegibilidade resultadoAvaliacao,
        String motivo,
        LocalDateTime dataAbertura,
        LocalDateTime dataPrazo,
        LocalDateTime dataAtualizacao,
        OrientacaoResponse orientacao
) {}
