package br.com.fiap.direitofacil.dto.response;

import br.com.fiap.direitofacil.enums.Role;

import java.time.LocalDateTime;

/** Conta de login sem a senha. */
public record UsuarioResponse(
        Long id,
        String nome,
        String cpf,
        String email,
        Role perfil,
        LocalDateTime dataCadastro
) {}
