package br.com.fiap.diretofacil.dto.response;

import br.com.fiap.diretofacil.enums.Role;

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
