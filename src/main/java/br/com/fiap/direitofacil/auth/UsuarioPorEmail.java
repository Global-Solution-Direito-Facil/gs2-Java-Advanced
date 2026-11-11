package br.com.fiap.direitofacil.auth;

import br.com.fiap.direitofacil.entity.Usuario;
import br.com.fiap.direitofacil.repository.UsuarioRepository;
import br.com.fiap.direitofacil.validation.EmailValidoValidator;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Acha o id do usuário pelo e-mail do token. É o que deixa o token emitido pela .NET
 * (sem a claim "usuarioId") funcionar nesta API; e-mail sem conta aqui continua sem acesso.
 */
@Component
@RequiredArgsConstructor
public class UsuarioPorEmail {

    private final UsuarioRepository repository;

    @PostConstruct
    void ligar() {
        CurrentUser.usarBuscaPorEmail(this::idDoEmail);
    }

    private Long idDoEmail(String email) {
        return repository.findByEmailIgnoreCase(EmailValidoValidator.normalizar(email))
                .map(Usuario::getId)
                .orElse(null);
    }
}
