package br.com.fiap.direitofacil.dto.response;

import br.com.fiap.direitofacil.enums.ResultadoFarmaciaPopular;

/** Resultado da consulta de um remédio ao elenco, sem gravar nada. */
public record ConsultaFarmaciaPopularResponse(
        String principioAtivo,
        String concentracao,
        ResultadoFarmaciaPopular resultado
) {}
