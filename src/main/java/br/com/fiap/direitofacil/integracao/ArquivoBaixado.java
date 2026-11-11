package br.com.fiap.direitofacil.integracao;

/** Arquivo de um documento baixado da API .NET, com o tipo de mídia e o nome original. */
public record ArquivoBaixado(
        byte[] conteudo,
        String tipoMidia,
        String nomeArquivo
) {
    public long tamanhoBytes() {
        return conteudo.length;
    }
}
