package br.com.fiap.direitofacil.exception;

import java.time.LocalDateTime;
import java.util.List;

/** Corpo padronizado de erro devolvido pela API em qualquer falha. */
public record ErroResponse(
        LocalDateTime timestamp,
        int status,
        String erro,
        String mensagem,
        String path,
        List<String> detalhes
) {}
