package br.com.fiap.direitofacil.dto.response;

import br.com.fiap.direitofacil.enums.ResultadoFarmaciaPopular;

/** Remédio conferido com o resultado decidido pela tabela do elenco e a orientação. */
public record ReceitaItemResponse(
        Long id,
        String nomeLido,
        String principioAtivo,
        String concentracao,
        Boolean corrigido,
        ResultadoFarmaciaPopular resultado,
        MedicamentoFarmaciaPopularResponse medicamento,
        String orientacao
) {}
