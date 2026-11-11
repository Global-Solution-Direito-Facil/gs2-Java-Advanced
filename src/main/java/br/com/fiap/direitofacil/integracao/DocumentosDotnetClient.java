package br.com.fiap.direitofacil.integracao;

import br.com.fiap.direitofacil.config.DotnetApiProperties;
import br.com.fiap.direitofacil.enums.StatusDocumentoDotnet;
import br.com.fiap.direitofacil.service.TokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Map;

/**
 * Chamadas do Java para a API .NET de documentos, sempre com um token de papel SERVICO
 * gerado na hora: baixar o arquivo (o storage é privado) e informar o resultado da leitura.
 */
@Slf4j
@Component
public class DocumentosDotnetClient {

    private static final long LIMITE_BYTES = 10L * 1024 * 1024;

    private final DotnetApiProperties api;
    private final TokenService tokenService;
    private final RestClient restClient;

    public DocumentosDotnetClient(DotnetApiProperties api, TokenService tokenService) {
        this.api = api;
        this.tokenService = tokenService;

        Duration timeout = Duration.ofSeconds(api.timeoutSegundos());
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(timeout).build());
        requestFactory.setReadTimeout(timeout);
        this.restClient = RestClient.builder().requestFactory(requestFactory).build();
    }

    /** Baixa o arquivo pelo GET /documentos/{id}/arquivo; 404 se a .NET não conhece o documento, 502 nas outras falhas. */
    public ArquivoBaixado baixarArquivo(String documentoId) {
        exigirConfigurada();
        try {
            ResponseEntity<byte[]> resposta = restClient.get()
                    .uri(api.urlDocumentos() + "/{id}/arquivo", documentoId)
                    .headers(this::autenticar)
                    .retrieve()
                    .toEntity(byte[].class);

            byte[] conteudo = resposta.getBody();
            if (conteudo == null || conteudo.length == 0) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "A API de documentos devolveu um arquivo vazio.");
            }
            if (conteudo.length > LIMITE_BYTES) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "O arquivo passa do limite de 10 MB.");
            }
            return new ArquivoBaixado(conteudo, tipoMidia(resposta.getHeaders()), nomeArquivo(resposta.getHeaders(), documentoId));
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == HttpStatus.NOT_FOUND.value()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Documento não encontrado na API de documentos.");
            }
            log.warn("API .NET respondeu {} ao baixar o documento {}", e.getStatusCode().value(), documentoId);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "A API de documentos recusou o download do arquivo.");
        } catch (RestClientException e) {
            log.warn("API .NET indisponível ao baixar o documento {}: {}", documentoId, e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "A API de documentos está indisponível.");
        }
    }

    /**
     * Informa o status pelo PATCH /documentos/{id}/status. Não lança erro: se a .NET estiver fora,
     * o documento só continua como RECEBIDO lá, e a leitura feita no Java não se perde.
     */
    public boolean informarStatus(String documentoId, StatusDocumentoDotnet status) {
        if (!api.configurada()) {
            log.warn("DOTNET_API_URL não definida; status {} do documento {} não foi informado", status, documentoId);
            return false;
        }
        try {
            restClient.patch()
                    .uri(api.urlDocumentos() + "/{id}/status", documentoId)
                    .headers(this::autenticar)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("status", status.name()))
                    .retrieve()
                    .toBodilessEntity();
            log.info("Documento {} marcado como {} na API .NET", documentoId, status);
            return true;
        } catch (RestClientException e) {
            log.warn("Não foi possível marcar o documento {} como {} na API .NET: {}", documentoId, status, e.getMessage());
            return false;
        }
    }

    private void autenticar(HttpHeaders headers) {
        headers.setBearerAuth(tokenService.gerarServico().valor());
    }

    private void exigirConfigurada() {
        if (!api.configurada()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "A API de documentos não está configurada (variável DOTNET_API_URL).");
        }
    }

    private String tipoMidia(HttpHeaders headers) {
        MediaType tipo = headers.getContentType();
        return tipo == null ? MediaType.APPLICATION_OCTET_STREAM_VALUE : tipo.getType() + "/" + tipo.getSubtype();
    }

    private String nomeArquivo(HttpHeaders headers, String documentoId) {
        String nome = headers.getContentDisposition().getFilename();
        return nome == null || nome.isBlank() ? documentoId : nome;
    }
}
