package br.com.fiap.direitofacil.ia;

import br.com.fiap.direitofacil.integracao.ArquivoBaixado;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeType;

import java.util.Set;

/**
 * A chamada ao Gemini (Spring AI multimodal) usada pelas leituras de laudo e de receita:
 * manda o prompt de sistema e o arquivo, e recebe a resposta já no formato do record pedido.
 */
@Slf4j
@Component
public class IaLeitorDocumento {

    private static final Set<String> TIPOS_ACEITOS = Set.of("application/pdf", "image/jpeg", "image/png");

    private final ChatClient chatClient;
    private final boolean configurada;

    public IaLeitorDocumento(ChatClient chatClient,
                             @Value("${spring.ai.google.genai.api-key:}") String chave) {
        this.chatClient = chatClient;
        this.configurada = chave != null && !chave.isBlank();
    }

    /** Lê o arquivo com o prompt informado; IaLeituraException se a IA está sem chave, fora do ar ou o tipo não é aceito. */
    public <T> T ler(String promptSistema, String pedido, ArquivoBaixado arquivo, Class<T> formato) {
        if (!configurada) {
            throw IaLeituraException.indisponivel("A leitura por IA não está configurada (variável GEMINI_API_KEY).", null);
        }
        if (!TIPOS_ACEITOS.contains(arquivo.tipoMidia())) {
            throw new IaLeituraException("Tipo de arquivo não aceito para leitura: " + arquivo.tipoMidia());
        }
        T lido;
        try {
            lido = chatClient.prompt()
                    .system(promptSistema)
                    .user(usuario -> usuario
                            .text(pedido)
                            .media(MimeType.valueOf(arquivo.tipoMidia()), new ByteArrayResource(arquivo.conteudo())))
                    .call()
                    .entity(formato);
        } catch (RuntimeException e) {
            log.warn("Falha na leitura por IA: {}", e.getMessage());
            throw IaLeituraException.indisponivel("A leitura por IA falhou. Tente novamente em instantes.", e);
        }
        if (lido == null) {
            throw IaLeituraException.indisponivel("A IA não devolveu a leitura do documento.", null);
        }
        return lido;
    }
}
