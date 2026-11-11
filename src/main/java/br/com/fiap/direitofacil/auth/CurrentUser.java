package br.com.fiap.direitofacil.auth;

import br.com.fiap.direitofacil.enums.Role;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.server.ResponseStatusException;

/** Lê role e id do usuário do JWT autenticado; sem token válido, nega em vez de liberar. */
public final class CurrentUser {

    private CurrentUser() {}

    private static Jwt jwt() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Jwt jwt)) {
            return null;
        }
        return jwt;
    }

    /** Email do usuário logado, ou null sem autenticação. */
    public static String email() {
        Jwt jwt = jwt();
        return jwt == null ? null : jwt.getSubject();
    }

    /** Papel do token, ou null se não houver token ou a claim for desconhecida. */
    public static Role role() {
        Jwt jwt = jwt();
        if (jwt == null) {
            return null;
        }
        String role = jwt.getClaimAsString("role");
        if (role == null) {
            return null;
        }
        try {
            return Role.valueOf(role);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /** Verdadeiro só com token de GESTOR; é a única condição que libera acesso amplo. */
    public static boolean isGestor() {
        return role() == Role.GESTOR;
    }

    public static boolean isCidadao() {
        return role() == Role.CIDADAO;
    }

    /** Id do usuário do token, ou null sem autenticação. */
    public static Long usuarioId() {
        Jwt jwt = jwt();
        if (jwt == null) {
            return null;
        }
        Object valor = jwt.getClaim("usuarioId");
        if (valor instanceof Number numero) {
            return numero.longValue();
        }
        if (valor instanceof String texto) {
            try {
                return Long.parseLong(texto);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /** Id do usuário do token, ou 401 se não houver; usado em todo filtro por dono. */
    public static Long requireUsuarioId() {
        Long id = usuarioId();
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não autenticado.");
        }
        return id;
    }

    /** Lança 403 se o token não for de GESTOR; usado nas operações exclusivas do gestor. */
    public static void requireGestor() {
        if (!isGestor()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Operação permitida apenas a gestores.");
        }
    }
}
