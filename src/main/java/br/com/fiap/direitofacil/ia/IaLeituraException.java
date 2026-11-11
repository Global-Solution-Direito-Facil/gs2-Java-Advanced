package br.com.fiap.direitofacil.ia;

/**
 * A leitura por IA não deu certo; a mensagem é o motivo. "indisponivel" separa a IA fora do ar
 * ou sem chave (tentar de novo depois) do documento que não pôde ser lido (enviar outro arquivo).
 */
public class IaLeituraException extends RuntimeException {

    private final boolean indisponivel;

    public IaLeituraException(String motivo) {
        super(motivo);
        this.indisponivel = false;
    }

    private IaLeituraException(String motivo, Throwable causa) {
        super(motivo, causa);
        this.indisponivel = true;
    }

    public static IaLeituraException indisponivel(String motivo, Throwable causa) {
        return new IaLeituraException(motivo, causa);
    }

    public boolean isIndisponivel() {
        return indisponivel;
    }
}
