package br.com.fiap.diretofacil.dto.response;

import br.com.fiap.diretofacil.enums.StatusDocumento;
import br.com.fiap.diretofacil.enums.TipoDocumento;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Documento com o status do processamento e os dados extraídos; nunca expõe o arquivo. */
public record DocumentoResponse(
        Long id,
        CidadaoResumoResponse cidadao,
        TipoDocumento tipo,
        String nomeArquivo,
        String contentType,
        StatusDocumento status,
        String condicao,
        String cid,
        Integer idadeExtraida,
        BigDecimal confiancaExtracao,
        String mensagemErro,
        LocalDateTime dataEnvio,
        LocalDateTime dataProcessamento
) {}
