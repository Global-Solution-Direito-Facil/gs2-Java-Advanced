package br.com.fiap.direitofacil.service;

import br.com.fiap.direitofacil.config.JwtProperties;
import br.com.fiap.direitofacil.entity.Usuario;
import br.com.fiap.direitofacil.enums.Role;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Contrato do token com a API .NET: HS256 com a chave em texto UTF-8, issuer e audience fixos, 8 horas, sub = e-mail, claims "email" e
 * "role" com CIDADAO ou GESTOR, sem o prefixo ROLE_.
 */
class TokenServiceTest {

    private static final String CHAVE = "chave-compartilhada-com-a-dotnet-123456";

    private final JwtProperties propriedades = new JwtProperties(CHAVE, null, null, null);
    private final SecretKeySpec chave = new SecretKeySpec(CHAVE.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    private final TokenService tokenService = new TokenService(new NimbusJwtEncoder(new ImmutableSecret<>(chave)), propriedades);

    private Usuario usuario(Role perfil) {
        return Usuario.builder().id(1L).nome("Ana Almeida").email("ana.almeida@email.com").perfil(perfil).build();
    }

    @Test
    void tokenSegueOContratoCombinadoComADotNet() {
        TokenService.Token token = tokenService.gerar(usuario(Role.CIDADAO));

        Jwt jwt = NimbusJwtDecoder.withSecretKey(chave).macAlgorithm(MacAlgorithm.HS256).build().decode(token.valor());

        assertThat(jwt.getHeaders().get("alg")).isEqualTo("HS256");
        assertThat(jwt.getSubject()).isEqualTo("ana.almeida@email.com");
        assertThat(jwt.getClaimAsString("iss")).isEqualTo("direitofacil");
        assertThat(jwt.getAudience()).containsExactly("direitofacil-app");
        assertThat(jwt.getClaimAsString("email")).isEqualTo("ana.almeida@email.com");
        assertThat(java.time.Duration.between(jwt.getIssuedAt(), jwt.getExpiresAt())).isEqualTo(java.time.Duration.ofHours(8));
        assertThat(jwt.getClaimAsString("role")).isEqualTo("CIDADAO");
        assertThat(jwt.getClaimAsString("role")).doesNotStartWith("ROLE_");
        assertThat(((Number) jwt.getClaim("usuarioId")).longValue()).isEqualTo(1L);
        assertThat(jwt.getExpiresAt()).isEqualTo(token.expiraEm());
    }

    @Test
    void tokenNaoValidaComOutraChave() {
        TokenService.Token token = tokenService.gerar(usuario(Role.GESTOR));
        SecretKeySpec outraChave = new SecretKeySpec("outra-chave-qualquer-com-32-caracteres!!".getBytes(StandardCharsets.UTF_8), "HmacSHA256");

        assertThatThrownBy(() -> NimbusJwtDecoder.withSecretKey(outraChave).macAlgorithm(MacAlgorithm.HS256).build().decode(token.valor()))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void chaveCurtaImpedeASubida() {
        assertThatThrownBy(() -> new JwtProperties("curta", null, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("JWT_SECRET");
    }
}
