package br.com.fiap.diretofacil.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Cadastro ou atualização de um beneficiário em POST e PUT /pessoas. */
public record PessoaRequest(
        @NotBlank @Size(max = 150) String nome,
        @NotNull @Past LocalDate dataNascimento,
        @NotNull @PositiveOrZero BigDecimal rendaFamiliar,
        @NotNull @Min(1) Integer qtdMembrosFamilia
) {}
