package br.com.fiap.direitofacil.ia;

import br.com.fiap.direitofacil.integracao.ArquivoBaixado;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;

import java.util.Set;

/**
 * Leitura do laudo médico com Spring AI (Gemini multimodal): recebe a foto ou o PDF e devolve
 * CID, condição, tipo e data em formato estruturado. A IA só extrai; não avalia direitos.
 */
@Slf4j
@Service
public class IaLaudoService {

    private static final Set<String> TIPOS_ACEITOS = Set.of("application/pdf", "image/jpeg", "image/png");

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

    private final ChatClient chatClient;
    private final boolean configurada;

    public IaLaudoService(ChatClient chatClient,
                               @Value("${spring.ai.google.genai.api-key:}") String chave) {
        this.chatClient = chatClient;
        this.configurada = chave != null && !chave.isBlank();
    }

    /** Envia o arquivo ao modelo e devolve a leitura; IaLeituraException se a IA não está disponível ou falha. */
    public LaudoLido ler(ArquivoBaixado arquivo) {
        if (!configurada) {
            throw new IaLeituraException("A leitura por IA não está configurada (variável GEMINI_API_KEY).");
        }
        if (!TIPOS_ACEITOS.contains(arquivo.tipoMidia())) {
            throw new IaLeituraException("Tipo de arquivo não aceito para leitura: " + arquivo.tipoMidia());
        }
        try {
            LaudoLido lido = chatClient.prompt()
                    .system(PROMPT_SISTEMA)
                    .user(usuario -> usuario
                            .text("Leia o laudo em anexo e preencha os campos.")
                            .media(MimeType.valueOf(arquivo.tipoMidia()), new ByteArrayResource(arquivo.conteudo())))
                    .call()
                    .entity(LaudoLido.class);
            if (lido == null) {
                throw new IaLeituraException("A IA não devolveu a leitura do laudo.");
            }
            return lido;
        } catch (IaLeituraException e) {
            throw e;
        } catch (RuntimeException e) {
            log.warn("Falha na leitura do laudo pela IA: {}", e.getMessage());
            throw new IaLeituraException("A leitura por IA falhou. Tente novamente em instantes.", e);
        }
    }
}
