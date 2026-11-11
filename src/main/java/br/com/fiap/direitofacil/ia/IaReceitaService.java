package br.com.fiap.direitofacil.ia;

import br.com.fiap.direitofacil.auth.CurrentUser;
import br.com.fiap.direitofacil.dto.response.MedicamentoLidoResponse;
import br.com.fiap.direitofacil.integracao.ArquivoBaixado;
import br.com.fiap.direitofacil.integracao.DocumentosDotnetClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Leitura da receita médica com Spring AI: devolve os remédios para a pessoa conferir. A IA não
 * diz se o remédio é gratuito e nunca sugere trocar remédio ou dosagem.
 */
@Service
@RequiredArgsConstructor
public class IaReceitaService {

    private static final String PROMPT_SISTEMA = """
            Você lê receitas médicas brasileiras para um serviço público de assistência social. \
            Sua única tarefa é transcrever os remédios que estão escritos na receita. \
            Nunca deduza, complete ou invente um remédio ou uma dosagem, nunca sugira trocar \
            remédio ou dosagem e nunca opine sobre o tratamento.

            Preencha os campos assim:
            - legivel: true somente se o arquivo for uma receita médica e der para ler.
            - medicamentos: um item para cada remédio ou insumo prescrito, na ordem da receita:
              - nome: o nome como está escrito na receita (comercial ou genérico).
              - principioAtivo: o princípio ativo, somente se estiver escrito na receita ou se o \
            nome escrito já for o do princípio ativo. Na dúvida, deixe vazio.
              - dosagem: a concentração como está escrita (exemplos: 50 mg, 100 UI/ml). Vazio se não houver.
            - observacao: se legivel for false, explique em uma frase curta o motivo; senão deixe vazio.
            """;

    private static final String PEDIDO = "Leia a receita em anexo e liste os remédios.";

    private final DocumentosDotnetClient dotnetClient;
    private final IaLeitorDocumento leitor;

    /**
     * Baixa a foto da receita na .NET com o token de quem pediu (assim a própria .NET recusa
     * documento de outra pessoa) e devolve os remédios lidos. Nada é gravado neste passo.
     */
    public List<MedicamentoLidoResponse> extrair(String documentoId) {
        ArquivoBaixado arquivo = dotnetClient.baixarArquivoComoUsuario(documentoId, CurrentUser.requireToken());
        return leitor.ler(PROMPT_SISTEMA, PEDIDO, arquivo, ReceitaLida.class).paraResponse();
    }
}
