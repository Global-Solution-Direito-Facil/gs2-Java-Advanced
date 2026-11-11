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

/**
 * Gera o JWT no formato combinado com a .NET: HS256, sub = e-mail e claim "role" com
 * CIDADAO ou GESTOR (sem ROLE_). A claim "usuarioId" é lida pelo CurrentUser deste projeto.
 */
@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtEncoder encoder;
    private final JwtProperties jwt;

    public record Token(String valor, Instant expiraEm) {}

    public Token gerar(Usuario usuario) {
        // o JWT guarda iat/exp em segundos; truncar deixa o expiraEm da resposta igual ao do token
        Instant agora = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        Instant expiraEm = agora.plus(jwt.expiracaoMinutos(), ChronoUnit.MINUTES);

        JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
                .issuedAt(agora)
                .expiresAt(expiraEm)
                .subject(usuario.getEmail())
                .claim("role", usuario.getPerfil().name())
                .claim("usuarioId", usuario.getId())
                .claim("nome", usuario.getNome());
        if (jwt.issuer() != null && !jwt.issuer().isBlank()) {
            claims.issuer(jwt.issuer());
        }

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).type("JWT").build();
        String valor = encoder.encode(JwtEncoderParameters.from(header, claims.build())).getTokenValue();
        return new Token(valor, expiraEm);
    }
}
