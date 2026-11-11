package br.com.fiap.direitofacil.enums;

/** Status do caso; o fluxo entre eles é validado pela trigger TRG_DF_CASO_VALIDA. */
public enum StatusCaso {
    ABERTO,
    EM_ANALISE,
    DOCUMENTOS_PENDENTES,
    PROTOCOLADO,
    CONCEDIDO,
    NEGADO
}
