package br.com.fiap.direitofacil.config;

import com.google.genai.Client;
import com.google.genai.types.HttpOptions;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.google.genai.embedding.GoogleGenAiEmbeddingConnectionDetails;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração do Spring AI com o Google Gemini. O Client é montado aqui para a chave ser
 * opcional: sem GEMINI_API_KEY a aplicação sobe e só a IA fica indisponível (a autoconfiguração
 * padrão impediria a subida). Chat e embeddings usam o mesmo Client e a mesma chave.
 */
@Configuration(proxyBeanMethods = false)
public class IaConfig {

    private static final String CHAVE_AUSENTE = "chave-nao-configurada";

    @Bean
    Client googleGenAiClient(@Value("${spring.ai.google.genai.api-key:}") String chave,
                             @Value("${direitofacil.ia.timeout-ms:60000}") int timeoutMs) {
        return Client.builder()
                .apiKey(chave == null || chave.isBlank() ? CHAVE_AUSENTE : chave.strip())
                .httpOptions(HttpOptions.builder().timeout(timeoutMs).build())
                .build();
    }

    @Bean
    GoogleGenAiEmbeddingConnectionDetails googleGenAiEmbeddingConnectionDetails(Client client) {
        return GoogleGenAiEmbeddingConnectionDetails.builder().genAiClient(client).build();
    }

    @Bean
    ChatClient chatClient(ChatClient.Builder builder) {
        return builder.defaultAdvisors(new SimpleLoggerAdvisor()).build();
    }

    /**
     * Vector store em memória da base de conhecimento do RAG. O Oracle 19c da FIAP não tem tipo
     * VECTOR, e a base é pequena (um texto por direito): ela é indexada na primeira consulta.
     */
    @Bean
    VectorStore baseConhecimentoVectorStore(EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }
}
