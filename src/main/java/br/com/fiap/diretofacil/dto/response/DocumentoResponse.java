package br.com.fiap.diretofacil.dto.response;

import br.com.fiap.diretofacil.enums.StatusDocumento;

import java.time.LocalDateTime;

/** Documento recebido com o status do processamento. */
public record DocumentoResponse(
        Long id,
        PessoaResumoResponse pessoa,
        String idExterno,
        String nomeArquivo,
        String tipoMidia,
        Long tamanhoBytes,
        StatusDocumento status,
        LocalDateTime dataRecebimento,
        LocalDateTime dataProcessamento
) {}
