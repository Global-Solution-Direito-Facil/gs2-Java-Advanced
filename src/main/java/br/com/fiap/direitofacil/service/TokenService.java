package br.com.fiap.direitofacil.service;

import br.com.fiap.direitofacil.config.JwtProperties;
import br.com.fiap.direitofacil.entity.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Gera o JWT no formato combinado com a .NET: HS256, issuer e audience iguais nas duas APIs,
 * sub = e-mail, claims "email" e "role" (CIDADAO ou GESTOR, sem ROLE_). As claims "usuarioId"
 * e "nome" são extras deste projeto; a .NET ignora.
 */
@Service
@RequiredArgsConstructor
public class TokenService {

    public static final String PAPEL_SERVICO = "SERVICO";
    public static final String IDENTIDADE_SERVICO = "servico-ia@direitofacil.app";
    private static final long VALIDADE_SERVICO_MINUTOS = 5L;

    private final JwtEncoder encoder;
    private final JwtProperties jwt;

    public record Token(String valor, Instant expiraEm) {}

    public Token gerar(Usuario usuario) {
        Instant agora = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        Instant expiraEm = agora.plus(jwt.expiracaoMinutos(), ChronoUnit.MINUTES);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(jwt.issuer())
                .audience(List.of(jwt.audience()))
                .issuedAt(agora)
                .expiresAt(expiraEm)
                .subject(usuario.getEmail())
                .claim("email", usuario.getEmail())
                .claim("role", usuario.getPerfil().name())
                .claim("usuarioId", usuario.getId())
                .claim("nome", usuario.getNome())
                .build();

        return new Token(assinar(claims), expiraEm);
    }

    /**
     * Token de papel SERVICO, usado só nas chamadas do Java para a .NET (baixar o arquivo e
     * informar o status). Vale 5 minutos, nunca sai do /auth/login e nunca passa pelo app.
     */
    public Token gerarServico() {
        Instant agora = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        Instant expiraEm = agora.plus(VALIDADE_SERVICO_MINUTOS, ChronoUnit.MINUTES);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(jwt.issuer())
                .audience(List.of(jwt.audience()))
                .issuedAt(agora)
                .expiresAt(expiraEm)
                .subject(IDENTIDADE_SERVICO)
                .claim("email", IDENTIDADE_SERVICO)
                .claim("role", PAPEL_SERVICO)
                .build();

        return new Token(assinar(claims), expiraEm);
    }

    private String assinar(JwtClaimsSet claims) {
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).type("JWT").build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
