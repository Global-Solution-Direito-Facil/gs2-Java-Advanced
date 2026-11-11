package br.com.fiap.direitofacil.dto.response;

/** Quantos eventos PrazoVencendo e CasoParado a verificação publicou, e com quais janelas. */
public record VerificacaoCasosResponse(
        int prazoVencendo,
        int casosParados,
        int diasPrazo,
        int diasParado
) {}
