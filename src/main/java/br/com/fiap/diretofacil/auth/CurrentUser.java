package br.com.fiap.diretofacil.auth;

import br.com.fiap.diretofacil.enums.Role;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

public final class CurrentUser {

    private CurrentUser() {}

    private static Jwt jwt() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Jwt jwt)) {
            return null;
        }
        return jwt;
    }

    public static String email() {
        Jwt jwt = jwt();
        return jwt == null ? null : jwt.getSubject();
    }

    public static Role role() {
        Jwt jwt = jwt();
        if (jwt == null) {
            return null;
        }
        String role = jwt.getClaimAsString("role");
        return role == null ? null : Role.valueOf(role);
    }

    public static boolean isGestor() {
        return role() == Role.GESTOR;
    }

    public static boolean isCidadao() {
        return role() == Role.CIDADAO;
    }

    public static Long perfilId() {
        Jwt jwt = jwt();
        if (jwt == null) {
            return null;
        }
        Object valor = jwt.getClaim("perfilId");
        return valor == null ? null : ((Number) valor).longValue();
    }
}
