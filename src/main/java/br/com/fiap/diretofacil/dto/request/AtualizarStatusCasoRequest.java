package br.com.fiap.diretofacil.dto.request;

import br.com.fiap.diretofacil.enums.StatusCaso;
import jakarta.validation.constraints.NotNull;

/** Movimentação do status de um caso pelo gestor em PATCH /casos/{id}/status. */
public record AtualizarStatusCasoRequest(
        @NotNull StatusCaso status
) {}
