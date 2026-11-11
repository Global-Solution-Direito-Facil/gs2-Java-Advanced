package br.com.fiap.direitofacil.ia;

/** A IA não conseguiu ler o documento, ou leu sem os dados obrigatórios; a mensagem é o motivo. */
public class IaLeituraException extends RuntimeException {

    public IaLeituraException(String motivo) {
        super(motivo);
    }

    public IaLeituraException(String motivo, Throwable causa) {
        super(motivo, causa);
    }
}
