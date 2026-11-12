package br.com.fiap.direitofacil.ia;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Base de conhecimento do RAG: os textos de src/main/resources/conhecimento (um trecho por
 * direito, com código e fonte) indexados no vector store. A indexação gera os embeddings no
 * Gemini, por isso acontece só na primeira busca, e não na subida da aplicação.
 */
@Slf4j
@Component
public class IaBaseConhecimento {

    public static final String CODIGO = "codigo";
    public static final String FONTE = "fonte";
    public static final String TITULO = "titulo";

    private static final String MARCA_TITULO = "# ";
    private static final String MARCA_CODIGO = "Código:";
    private static final String MARCA_FONTE = "Fonte:";

    private final VectorStore vectorStore;
    private final Resource arquivo;
    private volatile boolean indexada;

    public IaBaseConhecimento(VectorStore vectorStore,
                              @Value("classpath:conhecimento/direitos.md") Resource arquivo) {
        this.vectorStore = vectorStore;
        this.arquivo = arquivo;
    }

    /** Trechos mais parecidos com a pergunta; falha de embeddings vira IaLeituraException de indisponibilidade. */
    public List<Document> buscar(String pergunta, int quantidade) {
        try {
            indexarSeNecessario();
            return vectorStore.similaritySearch(SearchRequest.builder().query(pergunta).topK(quantidade).build());
        } catch (RuntimeException e) {
            log.warn("Falha na busca da base de conhecimento: {}", e.getMessage());
            throw IaLeituraException.indisponivel("A base de conhecimento da IA está indisponível. Tente novamente em instantes.", e);
        }
    }

    private void indexarSeNecessario() {
        if (indexada) {
            return;
        }
        synchronized (this) {
            if (!indexada) {
                List<Document> trechos = lerTrechos();
                vectorStore.add(trechos);
                indexada = true;
                log.info("Base de conhecimento indexada: {} trechos", trechos.size());
            }
        }
    }

    /** Divide o arquivo nos títulos "# ": cada direito vira um Document com código, fonte e título nos metadados. */
    List<Document> lerTrechos() {
        String conteudo;
        try {
            conteudo = arquivo.getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        List<Document> trechos = new ArrayList<>();
        String titulo = null;
        String codigo = "";
        String fonte = "";
        StringBuilder texto = new StringBuilder();

        for (String linha : conteudo.lines().toList()) {
            if (linha.startsWith(MARCA_TITULO)) {
                adicionar(trechos, titulo, codigo, fonte, texto);
                titulo = linha.substring(MARCA_TITULO.length()).strip();
                codigo = "";
                fonte = "";
                texto = new StringBuilder();
            } else if (linha.startsWith(MARCA_CODIGO)) {
                codigo = linha.substring(MARCA_CODIGO.length()).strip();
            } else if (linha.startsWith(MARCA_FONTE)) {
                fonte = linha.substring(MARCA_FONTE.length()).strip();
            } else if (titulo != null) {
                texto.append(linha).append('\n');
            }
        }
        adicionar(trechos, titulo, codigo, fonte, texto);
        return trechos;
    }

    private void adicionar(List<Document> trechos, String titulo, String codigo, String fonte, StringBuilder texto) {
        if (titulo == null || texto.toString().isBlank()) {
            return;
        }
        trechos.add(new Document(
                titulo + "\n" + texto.toString().strip(),
                Map.of(TITULO, titulo, CODIGO, codigo, FONTE, fonte)));
    }
}
