package br.com.fiap.diretofacil.dto.response;

/** Identificação curta do usuário, usada dentro de Pessoa. */
public record UsuarioResumoResponse(
        Long id,
        String nome,
        String email
) {}
