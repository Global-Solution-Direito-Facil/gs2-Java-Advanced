package br.com.fiap.direitofacil.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.charset.StandardCharsets;

/**
 * Configuração do JWT (prefixo "jwt" no application.properties).
 * A chave é compartilhada com a API .NET e usada como texto UTF-8 (getBytes), sem decodificar
 * Base64, para que os dois lados assinem e validem o mesmo token com HS256.
 */
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        String secret,
        String issuer,
        Long expiracaoMinutos
) {
    /** HS256 exige chave de pelo menos 256 bits (32 bytes). */
    private static final int TAMANHO_MINIMO_BYTES = 32;

    public JwtProperties {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < TAMANHO_MINIMO_BYTES) {
            throw new IllegalStateException(
                    "Defina a variavel de ambiente JWT_SECRET com pelo menos 32 caracteres (chave HS256 compartilhada com a .NET).");
        }
        if (expiracaoMinutos == null || expiracaoMinutos <= 0) {
            expiracaoMinutos = 60L;
        }
    }

    public byte[] secretBytes() {
        return secret.getBytes(StandardCharsets.UTF_8);
    }
}
