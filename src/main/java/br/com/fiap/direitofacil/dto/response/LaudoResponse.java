package br.com.fiap.direitofacil.dto.response;

import br.com.fiap.direitofacil.enums.TipoCondicao;

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
