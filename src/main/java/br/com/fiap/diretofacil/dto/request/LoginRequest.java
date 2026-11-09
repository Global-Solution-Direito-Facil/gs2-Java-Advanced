package br.com.fiap.diretofacil.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Credenciais enviadas em POST /auth/login. */
public record LoginRequest(
        @NotBlank @Email String email,
        @NotBlank String senha
) {}
