package br.com.fiap.diretofacil.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Atualização dos dados da conta; CPF, email e senha não mudam por aqui. */
public record UsuarioUpdateRequest(
        @NotBlank @Size(max = 150) String nome
) {}
