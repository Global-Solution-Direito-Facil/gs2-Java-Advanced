package br.com.fiap.diretofacil.dto.response;

/** Identificação curta do direito, usada dentro de Avaliação e Caso. */
public record DireitoResumoResponse(
        Long id,
        String codigo,
        String nome
) {}
