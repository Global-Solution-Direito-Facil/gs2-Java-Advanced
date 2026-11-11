package br.com.fiap.direitofacil.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Cadastro ou atualização de um beneficiário em POST e PUT /pessoas.
 * rendaIndividual é opcional: nulo = "não sei" no app; zero = sem renda própria.
 */
public record PessoaRequest(
        @NotBlank @Size(max = 150) String nome,
        @NotNull @Past LocalDate dataNascimento,
        @NotNull @PositiveOrZero BigDecimal rendaFamiliar,
        @NotNull @Min(1) Integer qtdMembrosFamilia,
        @PositiveOrZero BigDecimal rendaIndividual
) {
    /** Mesma regra do CHECK CK_DF_PESSOA_RENDA_IND: a renda individual não passa da renda familiar. */
    @AssertTrue(message = "rendaIndividual não pode ser maior que rendaFamiliar")
    public boolean isRendaIndividualDentroDaFamiliar() {
        return rendaIndividual == null || rendaFamiliar == null || rendaIndividual.compareTo(rendaFamiliar) <= 0;
    }
}
