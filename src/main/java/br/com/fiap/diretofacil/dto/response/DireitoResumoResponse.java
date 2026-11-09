package br.com.fiap.diretofacil.dto.response;

/** Identificação curta do direito, usada dentro de Caso e Avaliação. */
public record DireitoResumoResponse(
        Long id,
        String codigo,
        String nome,
        String orgao,
        String baseLegal
) {}
