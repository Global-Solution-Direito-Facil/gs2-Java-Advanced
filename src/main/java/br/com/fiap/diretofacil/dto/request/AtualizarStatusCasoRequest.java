package br.com.fiap.diretofacil.dto.request;

import br.com.fiap.diretofacil.enums.StatusCaso;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** Movimentação de um caso pelo gestor em PATCH /casos/{id}/status. */
public record AtualizarStatusCasoRequest(
        @NotNull StatusCaso status,
        @Size(max = 1000) String observacaoGestor,
        LocalDate prazo
) {}
