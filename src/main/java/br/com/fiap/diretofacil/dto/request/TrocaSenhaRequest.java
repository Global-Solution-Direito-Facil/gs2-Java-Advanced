package br.com.fiap.diretofacil.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Troca da própria senha em PATCH /auth/senha. */
public record TrocaSenhaRequest(
        @NotBlank String senhaAtual,
        @NotBlank @Size(min = 8, max = 72) String novaSenha
) {}
