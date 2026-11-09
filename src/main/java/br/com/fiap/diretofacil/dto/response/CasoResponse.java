package br.com.fiap.diretofacil.dto.response;

import br.com.fiap.diretofacil.enums.StatusCaso;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Caso de acompanhamento com cidadão, direito, gestor responsável e passo a passo. */
public record CasoResponse(
        Long id,
        CidadaoResumoResponse cidadao,
        DireitoResumoResponse direito,
        GestorResumoResponse gestorResponsavel,
        StatusCaso status,
        LocalDate prazo,
        String passoAPasso,
        String fontes,
        String observacaoGestor,
        LocalDateTime dataAbertura,
        LocalDateTime dataAtualizacao
) {}
