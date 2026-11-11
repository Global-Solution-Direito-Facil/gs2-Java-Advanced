package br.com.fiap.direitofacil.dto.response;

import br.com.fiap.direitofacil.enums.IndicacaoFarmaciaPopular;
import br.com.fiap.direitofacil.enums.TipoItemFarmaciaPopular;

import java.time.LocalDate;

/** Item do elenco oficial do Farmácia Popular, com a fonte e a data da lista. */
public record MedicamentoFarmaciaPopularResponse(
        Long id,
        IndicacaoFarmaciaPopular indicacao,
        TipoItemFarmaciaPopular tipoItem,
        String principioAtivo,
        String concentracao,
        String observacao,
        Boolean gratuito,
        String fonte,
        LocalDate dataElenco
) {}
