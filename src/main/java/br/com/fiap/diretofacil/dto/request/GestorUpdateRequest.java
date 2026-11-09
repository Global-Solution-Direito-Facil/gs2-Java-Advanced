package br.com.fiap.diretofacil.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Atualização de um gestor em PUT /gestores/{id}; CPF, matrícula e senha não mudam por aqui. */
public record GestorUpdateRequest(
        @NotBlank @Size(max = 150) String nome,
        @NotBlank @Size(max = 150) String unidade,
        @Size(max = 20) String telefone
) {}
