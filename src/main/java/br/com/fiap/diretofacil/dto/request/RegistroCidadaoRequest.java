package br.com.fiap.diretofacil.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Cadastro do cidadão pelo app em POST /auth/registrar.
 * Junta os dados do perfil (Cidadao) e da conta (Usuario) em um formulário só.
 */
public record RegistroCidadaoRequest(
        @NotBlank @Size(max = 150) String nome,
        @NotBlank @Pattern(regexp = "\\d{11}", message = "CPF deve ter 11 dígitos") String cpf,
        @NotNull @Past LocalDate dataNascimento,
        @NotNull @PositiveOrZero BigDecimal rendaFamiliar,
        @NotNull @Min(1) Integer qtdPessoasFamilia,
        @NotNull Boolean cadunico,
        @Size(max = 100) String municipio,
        @Size(min = 2, max = 2) String uf,
        @NotBlank @Email @Size(max = 150) String email,
        @NotBlank @Size(min = 8, max = 72) String senha
) {}
