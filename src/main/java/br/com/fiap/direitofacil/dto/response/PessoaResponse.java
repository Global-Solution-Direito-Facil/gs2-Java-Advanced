package br.com.fiap.direitofacil.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Beneficiário com idade e renda per capita calculadas e o responsável resumido; rendaIndividual nula = não informada. */
public record PessoaResponse(
        Long id,
        UsuarioResumoResponse responsavel,
        String nome,
        LocalDate dataNascimento,
        Integer idade,
        BigDecimal rendaFamiliar,
        Integer qtdMembrosFamilia,
        BigDecimal rendaPerCapita,
        BigDecimal rendaIndividual,
        LocalDateTime dataCadastro
) {}
