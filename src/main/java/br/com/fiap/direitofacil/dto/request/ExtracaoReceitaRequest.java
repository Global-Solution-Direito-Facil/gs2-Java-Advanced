package br.com.fiap.direitofacil.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** Pedido de leitura da receita em POST /receitas/extracao; documentoId é o id do arquivo na API .NET. */
public record ExtracaoReceitaRequest(
        @NotBlank
        @Pattern(regexp = "[A-Za-z0-9-]{1,100}", message = "documentoId inválido")
        String documentoId
) {}
