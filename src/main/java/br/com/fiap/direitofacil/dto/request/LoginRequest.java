package br.com.fiap.direitofacil.dto.request;

import br.com.fiap.direitofacil.validation.EmailValido;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Credenciais enviadas em POST /auth/login. */
public record LoginRequest(
        @NotBlank @EmailValido @Size(max = 150) String email,
        @NotBlank @Size(max = 72) String senha
) {}
