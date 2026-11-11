package br.com.fiap.direitofacil.ia;

import br.com.fiap.direitofacil.dto.response.MedicamentoLidoResponse;

import java.util.List;

/**
 * O que a IA leu da receita (saída estruturada do Spring AI). É só a transcrição: a pessoa
 * confere na tela e quem diz se o remédio é gratuito é a tabela do Farmácia Popular.
 */
public record ReceitaLida(
        boolean legivel,
        List<Medicamento> medicamentos,
        String observacao
) {
    private static final int TAMANHO_NOME = 150;
    private static final int TAMANHO_DOSAGEM = 60;

    public record Medicamento(String nome, String principioAtivo, String dosagem) {}

    /** Confere a leitura e devolve os remédios para a tela de conferência; receita ilegível ou vazia vira IaLeituraException. */
    public List<MedicamentoLidoResponse> paraResponse() {
        if (!legivel) {
            throw new IaLeituraException(observacao == null || observacao.isBlank()
                    ? "O arquivo não parece ser uma receita médica legível."
                    : observacao.strip());
        }
        List<MedicamentoLidoResponse> lidos = (medicamentos == null ? List.<Medicamento>of() : medicamentos).stream()
                .filter(remedio -> remedio != null && (temTexto(remedio.nome()) || temTexto(remedio.principioAtivo())))
                .map(remedio -> new MedicamentoLidoResponse(
                        cortar(remedio.nome(), TAMANHO_NOME),
                        cortar(remedio.principioAtivo(), TAMANHO_NOME),
                        cortar(remedio.dosagem(), TAMANHO_DOSAGEM)))
                .toList();
        if (lidos.isEmpty()) {
            throw new IaLeituraException("Nenhum remédio foi encontrado na receita.");
        }
        return lidos;
    }

    private static boolean temTexto(String texto) {
        return texto != null && !texto.isBlank();
    }

    private static String cortar(String texto, int tamanho) {
        if (texto == null) {
            return "";
        }
        String limpo = texto.strip();
        return limpo.length() <= tamanho ? limpo : limpo.substring(0, tamanho);
    }
}
