package br.com.fiap.diretofacil.dto.request;

import br.com.fiap.diretofacil.enums.TipoDocumento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Metadados do upload em POST /documentos; o arquivo vai como multipart ao lado. */
public record DocumentoRequest(
        @NotNull TipoDocumento tipo,
        @NotBlank @Size(max = 100) String idempotencyKey
) {}
