package br.com.fiap.diretofacil.dto.response;

/** Identificação curta do gestor, usada dentro de Caso. */
public record GestorResumoResponse(
        Long id,
        String nome,
        String unidade
) {}
