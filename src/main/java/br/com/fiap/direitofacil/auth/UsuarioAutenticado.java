package br.com.fiap.direitofacil.auth;

import br.com.fiap.direitofacil.enums.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/** Usuário carregado no login; a authority é o próprio perfil (CIDADAO ou GESTOR), sem prefixo ROLE_. */
public record UsuarioAutenticado(
        Long id,
        String email,
        String senha,
        Role role
) implements UserDetails {

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getPassword() {
        return senha;
    }

    @Override
    public String getUsername() {
        return email;
    }
}
