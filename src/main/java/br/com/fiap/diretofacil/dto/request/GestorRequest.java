package br.com.fiap.diretofacil.dto.request;

import jakarta.validation.constraints.*;

/** Criação de um gestor em POST /gestores, com perfil e conta de login. */
public record GestorRequest(
        @NotBlank @Size(max = 150) String nome,
        @NotBlank @Pattern(regexp = "\\d{11}", message = "CPF deve ter 11 dígitos") String cpf,
        @NotBlank @Size(max = 30) String matricula,
        @NotBlank @Size(max = 150) String unidade,
        @Size(max = 20) String telefone,
        @NotBlank @Email @Size(max = 150) String email,
        @NotBlank @Size(min = 8, max = 72) String senha
) {}
