package br.com.fiap.direitofacil.dto.response;

/** Identificação curta do beneficiário, usada dentro de Documento, Laudo e Caso. */
public record PessoaResumoResponse(
        Long id,
        String nome
) {}
