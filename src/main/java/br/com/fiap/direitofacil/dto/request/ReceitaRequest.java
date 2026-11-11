package br.com.fiap.direitofacil.dto.request;

import jakarta.validation.constraints.NotNull;

/** Abertura de uma receita para a consulta do Farmácia Popular; documentoId é a foto já registrada (opcional). */
public record ReceitaRequest(
        @NotNull Long pessoaId,
        Long documentoId
) {}
