package br.com.fiap.direitofacil.integracao;

import br.com.fiap.direitofacil.config.DotnetApiProperties;
import br.com.fiap.direitofacil.config.JwtProperties;
import br.com.fiap.direitofacil.enums.StatusDocumentoDotnet;
import br.com.fiap.direitofacil.service.TokenService;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Contrato das chamadas para a .NET, contra um servidor HTTP local: rotas, token SERVICO no
 * cabeçalho, corpo do PATCH e o comportamento quando a .NET falha ou não está configurada.
 */
class DocumentosDotnetClientTest {

    private static final String CHAVE = "chave-compartilhada-com-a-dotnet-123456";
    private static final String DOCUMENTO = "3f2504e0-4f89-11d3-9a0c-0305e82c3301";

    private final SecretKeySpec chave = new SecretKeySpec(CHAVE.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    private final TokenService tokenService = new TokenService(
            new NimbusJwtEncoder(new ImmutableSecret<>(chave)), new JwtProperties(CHAVE, null, null, null));

    private final AtomicReference<String> metodo = new AtomicReference<>();
    private final AtomicReference<String> caminho = new AtomicReference<>();
    private final AtomicReference<String> autorizacao = new AtomicReference<>();
    private final AtomicReference<String> corpo = new AtomicReference<>();

    private HttpServer servidor;
    private int statusResposta = 200;

    @BeforeEach
    void subirServidor() throws IOException {
        servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        servidor.createContext("/", troca -> {
            metodo.set(troca.getRequestMethod());
            caminho.set(troca.getRequestURI().getPath());
            autorizacao.set(troca.getRequestHeaders().getFirst("Authorization"));
            corpo.set(new String(troca.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));

            if (statusResposta != 200) {
                troca.sendResponseHeaders(statusResposta, -1);
            } else if ("GET".equals(troca.getRequestMethod())) {
                byte[] arquivo = "%PDF-laudo".getBytes(StandardCharsets.UTF_8);
                troca.getResponseHeaders().add("Content-Type", "application/pdf");
                troca.getResponseHeaders().add("Content-Disposition", "attachment; filename=\"laudo_lucas.pdf\"");
                troca.sendResponseHeaders(200, arquivo.length);
                troca.getResponseBody().write(arquivo);
            } else {
                troca.sendResponseHeaders(204, -1);
            }
            troca.close();
        });
        servidor.start();
    }

    @AfterEach
    void derrubarServidor() {
        servidor.stop(0);
    }

    private DocumentosDotnetClient cliente() {
        String url = "http://127.0.0.1:" + servidor.getAddress().getPort() + "/";
        return new DocumentosDotnetClient(new DotnetApiProperties(url, "api/v1", 5), tokenService);
    }

    private Jwt tokenRecebido() {
        assertThat(autorizacao.get()).startsWith("Bearer ");
        return NimbusJwtDecoder.withSecretKey(chave).macAlgorithm(MacAlgorithm.HS256).build()
                .decode(autorizacao.get().substring("Bearer ".length()));
    }

    @Test
    void baixaOArquivoComTokenDeServico() {
        ArquivoBaixado arquivo = cliente().baixarArquivo(DOCUMENTO);

        assertThat(metodo.get()).isEqualTo("GET");
        assertThat(caminho.get()).isEqualTo("/api/v1/documentos/" + DOCUMENTO + "/arquivo");
        assertThat(tokenRecebido().getClaimAsString("role")).isEqualTo("SERVICO");
        assertThat(new String(arquivo.conteudo(), StandardCharsets.UTF_8)).isEqualTo("%PDF-laudo");
        assertThat(arquivo.tipoMidia()).isEqualTo("application/pdf");
        assertThat(arquivo.nomeArquivo()).isEqualTo("laudo_lucas.pdf");
        assertThat(arquivo.tamanhoBytes()).isEqualTo(10);
    }

    @Test
    void baixaComOTokenDoUsuarioSemTrocarPeloDeServico() {
        cliente().baixarArquivoComoUsuario(DOCUMENTO, "token-da-ana");

        assertThat(autorizacao.get()).isEqualTo("Bearer token-da-ana");
        assertThat(caminho.get()).isEqualTo("/api/v1/documentos/" + DOCUMENTO + "/arquivo");
    }

    @Test
    void documentoDeOutroDonoViraNotFound() {
        statusResposta = 403;

        assertThatThrownBy(() -> cliente().baixarArquivoComoUsuario(DOCUMENTO, "token-da-ana"))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void informaOStatusEmMaiusculasPeloPatch() {
        boolean informado = cliente().informarStatus(DOCUMENTO, StatusDocumentoDotnet.EXTRAIDO);

        assertThat(informado).isTrue();
        assertThat(metodo.get()).isEqualTo("PATCH");
        assertThat(caminho.get()).isEqualTo("/api/v1/documentos/" + DOCUMENTO + "/status");
        assertThat(corpo.get()).isEqualTo("{\"status\":\"EXTRAIDO\"}");
        assertThat(tokenRecebido().getClaimAsString("role")).isEqualTo("SERVICO");
    }

    @Test
    void documentoDesconhecidoNaDotnetViraNotFound() {
        statusResposta = 404;

        assertThatThrownBy(() -> cliente().baixarArquivo(DOCUMENTO))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void falhaDaDotnetNoDownloadViraBadGateway() {
        statusResposta = 500;

        assertThatThrownBy(() -> cliente().baixarArquivo(DOCUMENTO))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY));
    }

    @Test
    void falhaAoInformarStatusNaoLancaErro() {
        statusResposta = 500;

        assertThat(cliente().informarStatus(DOCUMENTO, StatusDocumentoDotnet.FALHA)).isFalse();
    }

    @Test
    void semUrlConfiguradaNaoChamaADotnet() {
        DocumentosDotnetClient semUrl = new DocumentosDotnetClient(new DotnetApiProperties("", null, null), tokenService);

        assertThat(semUrl.informarStatus(DOCUMENTO, StatusDocumentoDotnet.EXTRAIDO)).isFalse();
        assertThatThrownBy(() -> semUrl.baixarArquivo(DOCUMENTO))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE));
        assertThat(metodo.get()).isNull();
    }
}
