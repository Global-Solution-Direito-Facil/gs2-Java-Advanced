package br.com.fiap.diretofacil.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Perfil completo do cidadão, com idade e renda per capita calculadas e o email da conta. */
public record CidadaoResponse(
        Long id,
        String nome,
        String cpf,
        LocalDate dataNascimento,
        Integer idade,
        BigDecimal rendaFamiliar,
        Integer qtdPessoasFamilia,
        BigDecimal rendaPerCapita,
        Boolean cadunico,
        String municipio,
        String uf,
        String email,
        LocalDateTime dataCadastro
) {}
