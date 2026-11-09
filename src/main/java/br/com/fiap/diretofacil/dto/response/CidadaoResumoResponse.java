package br.com.fiap.diretofacil.dto.response;

/** Identificação curta do cidadão, usada dentro de Documento e Caso. */
public record CidadaoResumoResponse(
        Long id,
        String nome,
        String cpf
) {}
