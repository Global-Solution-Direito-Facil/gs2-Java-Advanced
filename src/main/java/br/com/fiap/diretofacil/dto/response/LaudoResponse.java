package br.com.fiap.diretofacil.dto.response;

import br.com.fiap.diretofacil.enums.TipoCondicao;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Dados estruturados de um laudo. */
public record LaudoResponse(
        Long id,
        PessoaResumoResponse pessoa,
        Long documentoId,
        String codCid,
        String condicao,
        TipoCondicao tipoCondicao,
        LocalDate dataLaudo,
        LocalDateTime dataCadastro
) {}
