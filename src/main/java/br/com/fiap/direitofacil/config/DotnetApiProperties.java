package br.com.fiap.direitofacil.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Endereço da API .NET de documentos (prefixo "dotnet.api" no application.properties).
 * Sem a URL a aplicação sobe normalmente; só as chamadas para a .NET ficam indisponíveis.
 */
@ConfigurationProperties(prefix = "dotnet.api")
public record DotnetApiProperties(
        String url,
        String prefixo,
        Integer timeoutSegundos
) {
    public DotnetApiProperties {
        url = url == null ? "" : url.strip().replaceAll("/+$", "");
        prefixo = prefixo == null || prefixo.isBlank() ? "/api/v1" : "/" + prefixo.strip().replaceAll("^/+|/+$", "");
        if (timeoutSegundos == null || timeoutSegundos <= 0) {
            timeoutSegundos = 30;
        }
    }

    public boolean configurada() {
        return !url.isBlank();
    }

    /** URL base das rotas de documentos, por exemplo https://host/api/v1/documentos. */
    public String urlDocumentos() {
        return url + prefixo + "/documentos";
    }
}
