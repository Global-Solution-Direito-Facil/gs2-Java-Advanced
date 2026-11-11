package br.com.fiap.direitofacil.ia;

import br.com.fiap.direitofacil.dto.request.LaudoRequest;
import br.com.fiap.direitofacil.enums.TipoCondicao;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * O que a IA leu do laudo (saída estruturada do Spring AI). A IA só extrai: quem decide a
 * elegibilidade é a regra PL/SQL, a partir do laudo gravado com estes dados.
 */
public record LaudoLido(
        boolean legivel,
        String codCid,
        String condicao,
        TipoCondicao tipoCondicao,
        String dataLaudo,
        String observacao
) {
    private static final int TAMANHO_CID = 10;
    private static final int TAMANHO_CONDICAO = 200;

    /** Confere a leitura e monta o registro do laudo; leitura incompleta vira IaLeituraException com o motivo. */
    public LaudoRequest paraRequest(Long pessoaId, Long documentoId) {
        if (!legivel) {
            throw new IaLeituraException(observacao == null || observacao.isBlank()
                    ? "O arquivo não parece ser um laudo médico legível."
                    : observacao.strip());
        }
        if (codCid == null || codCid.isBlank()) {
            throw new IaLeituraException("O laudo não informa o código CID.");
        }
        String cid = codCid.strip().toUpperCase();
        if (cid.length() > TAMANHO_CID) {
            throw new IaLeituraException("O código CID lido não é válido: " + cid);
        }
        if (condicao == null || condicao.isBlank()) {
            throw new IaLeituraException("O laudo não descreve a condição.");
        }
        String descricao = condicao.strip();
        if (descricao.length() > TAMANHO_CONDICAO) {
            descricao = descricao.substring(0, TAMANHO_CONDICAO);
        }
        return new LaudoRequest(
                pessoaId,
                documentoId,
                cid,
                descricao,
                tipoCondicao == null ? TipoCondicao.OUTRA : tipoCondicao,
                data()
        );
    }

    private LocalDate data() {
        if (dataLaudo == null || dataLaudo.isBlank()) {
            throw new IaLeituraException("O laudo não informa a data de emissão.");
        }
        try {
            LocalDate data = LocalDate.parse(dataLaudo.strip());
            if (data.isAfter(LocalDate.now())) {
                throw new IaLeituraException("A data do laudo está no futuro: " + dataLaudo);
            }
            return data;
        } catch (DateTimeParseException e) {
            throw new IaLeituraException("A data do laudo não pôde ser lida: " + dataLaudo);
        }
    }
}
