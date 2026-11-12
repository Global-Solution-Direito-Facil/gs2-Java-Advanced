package br.com.fiap.direitofacil.ia;

import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** A base de conhecimento cobre todos os direitos do catálogo do banco, cada um com código e fonte. */
class IaBaseConhecimentoTest {

    private final VectorStore vectorStore = mock(VectorStore.class);
    private final IaBaseConhecimento base = new IaBaseConhecimento(vectorStore, new ClassPathResource("conhecimento/direitos.md"));

    @Test
    void cadaTrechoTemTituloCodigoFonteEPassos() {
        List<Document> trechos = base.lerTrechos();

        assertThat(trechos).isNotEmpty().allSatisfy(trecho -> {
            assertThat((String) trecho.getMetadata().get(IaBaseConhecimento.TITULO)).isNotBlank();
            assertThat((String) trecho.getMetadata().get(IaBaseConhecimento.CODIGO)).matches("[A-Z0-9_]+");
            assertThat((String) trecho.getMetadata().get(IaBaseConhecimento.FONTE)).isNotBlank();
            assertThat(trecho.getText()).contains("Como pedir:");
        });
    }

    @Test
    void todoDireitoDoCatalogoDoBancoTemOrientacaoNaBase() throws IOException {
        String carga = new ClassPathResource("db/migration/V3__carga_inicial.sql").getContentAsString(StandardCharsets.UTF_8);
        Set<String> doBanco = new TreeSet<>();
        Matcher direito = Pattern.compile("PRC_CARGA_DIREITO\\('([A-Z0-9_]+)'").matcher(carga);
        while (direito.find()) {
            doBanco.add(direito.group(1));
        }

        Set<String> daBase = base.lerTrechos().stream()
                .map(trecho -> (String) trecho.getMetadata().get(IaBaseConhecimento.CODIGO))
                .collect(Collectors.toCollection(TreeSet::new));

        assertThat(doBanco).hasSize(20);
        assertThat(daBase).isEqualTo(doBanco);
    }

    @Test
    void indexaSoNaPrimeiraBusca() {
        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of());

        base.buscar("Como pedir o BPC", 3);
        base.buscar("Como pedir a CIPTEA", 3);

        verify(vectorStore, times(1)).add(anyList());
        verify(vectorStore, times(2)).similaritySearch(any(SearchRequest.class));
    }

    @Test
    void falhaDosEmbeddingsViraIndisponibilidade() {
        doThrow(new IllegalStateException("sem chave")).when(vectorStore).add(anyList());

        assertThatThrownBy(() -> base.buscar("Como pedir o BPC", 3))
                .isInstanceOfSatisfying(IaLeituraException.class, e -> assertThat(e.isIndisponivel()).isTrue());
    }
}
