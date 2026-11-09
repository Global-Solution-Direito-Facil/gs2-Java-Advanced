package br.com.fiap.diretofacil.dto.response;

import br.com.fiap.diretofacil.enums.Role;

/** Token JWT e dados mínimos da conta devolvidos no login e no registro. */
public record LoginResponse(
        String token,
        String tipo,
        String expiraEm,
        Role role,
        Long perfilId
) {}
