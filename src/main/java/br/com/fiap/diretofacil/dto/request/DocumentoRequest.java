package br.com.fiap.diretofacil.dto.request;

import jakarta.validation.constraints.*;

/** Registro de um documento recebido; o arquivo fica na API .NET, aqui só os metadados. */
public record DocumentoRequest(
        @NotNull Long pessoaId,
        @NotBlank @Size(max = 100) String idExterno,
        @NotBlank @Size(max = 200) String nomeArquivo,
        @NotBlank @Size(max = 50) String tipoMidia,
        @NotNull @Positive Long tamanhoBytes,
        @NotBlank @Size(max = 100) String chaveIdempotencia
) {}
