package br.com.fiap.direitofacil.dto.response;

/** Um remédio que a IA leu na foto da receita, para a pessoa conferir e corrigir na tela. */
public record MedicamentoLidoResponse(
        String nome,
        String principioAtivo,
        String dosagem
) {}
