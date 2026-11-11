package br.com.fiap.direitofacil.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.charset.StandardCharsets;

/**
 * Configuração do JWT (prefixo "jwt" no application.properties), igual à da API .NET:
 * HS256 com a chave em texto UTF-8 (getBytes, sem decodificar Base64), issuer "direitofacil",
 * audience "direitofacil-app" e token de usuário valendo 8 horas.
 */
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        String secret,
        String issuer,
        String audience,
        Long expiracaoMinutos
) {
    /** HS256 exige chave de pelo menos 256 bits (32 bytes). */
    private static final int TAMANHO_MINIMO_BYTES = 32;

    public static final String ISSUER_PADRAO = "direitofacil";
    public static final String AUDIENCE_PADRAO = "direitofacil-app";
    public static final long EXPIRACAO_PADRAO_MINUTOS = 480L;

    public JwtProperties {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < TAMANHO_MINIMO_BYTES) {
            throw new IllegalStateException(
                    "Defina a variavel de ambiente JWT_SECRET com pelo menos 32 caracteres (chave HS256 compartilhada com a .NET).");
        }
        if (issuer == null || issuer.isBlank()) {
            issuer = ISSUER_PADRAO;
        }
        if (audience == null || audience.isBlank()) {
            audience = AUDIENCE_PADRAO;
        }
        if (expiracaoMinutos == null || expiracaoMinutos <= 0) {
            expiracaoMinutos = EXPIRACAO_PADRAO_MINUTOS;
        }
    }

    public byte[] secretBytes() {
        return secret.getBytes(StandardCharsets.UTF_8);
    }
}
