package br.com.fiap.direitofacil.dto.response;

import java.time.LocalDateTime;
import java.util.List;

/** Receita com os remédios conferidos e quantos são gratuitos. */
public record ReceitaResponse(
        Long id,
        PessoaResumoResponse pessoa,
        Long documentoId,
        LocalDateTime dataRegistro,
        int totalItens,
        int totalGratuitos,
        List<ReceitaItemResponse> itens
) {}
