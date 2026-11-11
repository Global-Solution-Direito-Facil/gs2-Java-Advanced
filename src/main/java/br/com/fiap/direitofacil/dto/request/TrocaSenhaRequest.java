package br.com.fiap.direitofacil.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Troca da própria senha em PATCH /auth/senha. */
public record TrocaSenhaRequest(
        @NotBlank @Size(max = 72) String senhaAtual,
        @NotBlank @Size(min = 8, max = 72) String novaSenha
) {}
