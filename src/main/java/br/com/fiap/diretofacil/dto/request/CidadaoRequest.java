package br.com.fiap.diretofacil.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Dados do perfil do cidadão, usados pelo gestor em POST e PUT /cidadaos. */
public record CidadaoRequest(
        @NotBlank @Size(max = 150) String nome,
        @NotBlank @Pattern(regexp = "\\d{11}", message = "CPF deve ter 11 dígitos") String cpf,
        @NotNull @Past LocalDate dataNascimento,
        @NotNull @PositiveOrZero BigDecimal rendaFamiliar,
        @NotNull @Min(1) Integer qtdPessoasFamilia,
        @NotNull Boolean cadunico,
        @Size(max = 100) String municipio,
        @Size(min = 2, max = 2) String uf
) {}
