package br.com.fiap.direitofacil.auth;

import br.com.fiap.direitofacil.enums.Role;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.List;
import java.util.function.Supplier;

/**
 * Executa um trecho com a identidade interna da leitura por IA. O consumidor da fila roda fora
 * de uma requisição HTTP, sem usuário logado, e os services exigem um papel: aqui a IA grava o
 * laudo com a mesma permissão do gestor. Essa identidade existe só na memória desta thread,
 * nunca vira token e é removida ao final.
 */
public final class ContextoInterno {

    public static final String IDENTIDADE = "leitura-ia@direitofacil.app";

    private ContextoInterno() {}

    public static <T> T executar(Supplier<T> acao) {
        SecurityContext anterior = SecurityContextHolder.getContext();
        try {
            Instant agora = Instant.now();
            Jwt identidade = Jwt.withTokenValue("interno")
                    .header("alg", "none")
                    .subject(IDENTIDADE)
                    .claim("role", Role.GESTOR.name())
                    .issuedAt(agora)
                    .expiresAt(agora.plusSeconds(60))
                    .build();
            SecurityContext contexto = SecurityContextHolder.createEmptyContext();
            contexto.setAuthentication(new JwtAuthenticationToken(
                    identidade, List.of(new SimpleGrantedAuthority(Role.GESTOR.name()))));
            SecurityContextHolder.setContext(contexto);
            return acao.get();
        } finally {
            SecurityContextHolder.setContext(anterior);
        }
    }
}
