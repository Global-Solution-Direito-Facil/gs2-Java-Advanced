package br.com.fiap.direitofacil.ia;

import br.com.fiap.direitofacil.dto.request.RegistrarOrientacaoRequest;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * O passo a passo que a IA escreveu a partir dos trechos da base de conhecimento (saída
 * estruturada). "encontrada" é false quando os trechos não tratam do direito do caso.
 */
public record OrientacaoGerada(
        boolean encontrada,
        List<String> passos
) {
    private static final int LIMITE_PASSO_A_PASSO_BYTES = 4000;
    private static final int LIMITE_FONTE_BYTES = 500;
    private static final int LIMITE_MODELO = 100;

    /**
     * Monta o registro da orientação: um passo por linha (é como o app exibe), a fonte vinda dos
     * trechos usados (nunca escrita pela IA) e o nome do modelo. Sem passos, IaLeituraException.
     */
    public RegistrarOrientacaoRequest paraRequest(String fonte, String modelo) {
        List<String> linhas = (passos == null ? List.<String>of() : passos).stream()
                .filter(passo -> passo != null && !passo.isBlank())
                .map(passo -> passo.strip().replaceAll("\\s*\\R\\s*", " "))
                .toList();
        if (!encontrada || linhas.isEmpty()) {
            throw new IaLeituraException("A base de conhecimento não tem orientação para este direito.");
        }
        if (fonte == null || fonte.isBlank()) {
            throw new IaLeituraException("A orientação não tem fonte para citar.");
        }

        StringBuilder texto = new StringBuilder();
        for (String linha : linhas) {
            String candidato = texto.isEmpty() ? linha : texto + "\n" + linha;
            if (bytes(candidato) > LIMITE_PASSO_A_PASSO_BYTES) {
                break;
            }
            texto = new StringBuilder(candidato);
        }
        if (texto.isEmpty()) {
            throw new IaLeituraException("O passo a passo gerado passou do tamanho permitido.");
        }
        return new RegistrarOrientacaoRequest(
                texto.toString(),
                cortarEmBytes(fonte.strip(), LIMITE_FONTE_BYTES),
                modelo == null || modelo.length() <= LIMITE_MODELO ? modelo : modelo.substring(0, LIMITE_MODELO));
    }

    private static int bytes(String texto) {
        return texto.getBytes(StandardCharsets.UTF_8).length;
    }

    private static String cortarEmBytes(String texto, int limite) {
        String cortado = texto;
        while (bytes(cortado) > limite) {
            cortado = cortado.substring(0, cortado.length() - 1);
        }
        return cortado;
    }
}
