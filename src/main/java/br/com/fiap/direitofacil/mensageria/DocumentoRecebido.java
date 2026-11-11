package br.com.fiap.direitofacil.mensageria;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Evento publicado pela API .NET na fila documentos.recebidos quando um arquivo chega.
 * cidadaoId é o e-mail de quem enviou; pessoaId (o beneficiário) é opcional enquanto a
 * .NET não repassar o campo que o app manda no upload.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record DocumentoRecebido(
        String eventId,
        String documentoId,
        String cidadaoId,
        Long pessoaId,
        String tipoDocumento,
        String contentType,
        String criadoEm
) {
    public boolean ehLaudo() {
        return "LAUDO".equalsIgnoreCase(tipoDocumento);
    }
}
