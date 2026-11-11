package br.com.fiap.direitofacil.ia;

import br.com.fiap.direitofacil.integracao.ArquivoBaixado;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Leitura do laudo médico com Spring AI: recebe a foto ou o PDF e devolve CID, condição, tipo
 * e data em formato estruturado. A IA só extrai; não avalia direitos.
 */
@Service
@RequiredArgsConstructor
public class IaLaudoService {

    private static final String PROMPT_SISTEMA = """
            Você lê laudos médicos brasileiros para um serviço público de assistência social. \
            Sua única tarefa é transcrever dados que estão escritos no documento. \
            Nunca deduza, complete ou invente uma informação que não esteja no laudo, \
            e nunca opine sobre direitos, benefícios ou tratamento.

            Preencha os campos assim:
            - legivel: true somente se o arquivo for um laudo, relatório ou atestado médico e der para ler.
            - codCid: o código CID principal como está escrito (exemplos: F84.0, G80, H54.0). Vazio se não houver.
            - condicao: o nome da condição ou do diagnóstico, em até 200 caracteres.
            - tipoCondicao: classifique pelo diagnóstico escrito no laudo:
              TEA para transtorno do espectro autista (CID F84 ou 6A02);
              DEF_FISICA para deficiência física ou motora;
              DEF_VISUAL para cegueira ou baixa visão;
              DEF_AUDITIVA para surdez ou perda auditiva;
              DEF_INTELECTUAL para deficiência intelectual;
              DEF_MULTIPLA quando o laudo descreve mais de uma deficiência;
              OUTRA para qualquer outro caso.
            - dataLaudo: a data de emissão do laudo no formato AAAA-MM-DD. Vazio se não houver.
            - observacao: se legivel for false, explique em uma frase curta o motivo; senão deixe vazio.
            """;

    private static final String PEDIDO = "Leia o laudo em anexo e preencha os campos.";

    private final IaLeitorDocumento leitor;

    /** Envia o arquivo ao modelo e devolve a leitura do laudo. */
    public LaudoLido ler(ArquivoBaixado arquivo) {
        return leitor.ler(PROMPT_SISTEMA, PEDIDO, arquivo, LaudoLido.class);
    }
}
