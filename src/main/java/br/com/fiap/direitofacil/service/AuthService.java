package br.com.fiap.direitofacil.service;

import br.com.fiap.direitofacil.auth.CurrentUser;
import br.com.fiap.direitofacil.auth.UsuarioAutenticado;
import br.com.fiap.direitofacil.dto.request.TrocaSenhaRequest;
import br.com.fiap.direitofacil.dto.request.UsuarioRequest;
import br.com.fiap.direitofacil.dto.response.LoginResponse;
import br.com.fiap.direitofacil.entity.Usuario;
import br.com.fiap.direitofacil.enums.Role;
import br.com.fiap.direitofacil.repository.UsuarioRepository;
import br.com.fiap.direitofacil.validation.EmailValidoValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** Login, registro do cidadão e troca de senha; também é o UserDetailsService do Spring Security. */
@Service
@RequiredArgsConstructor
public class AuthService implements UserDetailsService {

    private final UsuarioRepository repository;
    private final UsuarioService usuarioService;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;

    /** Carrega a conta pelo e-mail (sem diferenciar maiúsculas) para o AuthenticationManager conferir a senha. */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = repository.findByEmailIgnoreCase(EmailValidoValidator.normalizar(email))
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado."));
        return new UsuarioAutenticado(usuario.getId(), usuario.getEmail(), usuario.getSenha(), usuario.getPerfil());
    }

    /** Gera o token de quem já passou pelo AuthenticationManager no AuthController. */
    @Transactional(readOnly = true)
    public LoginResponse tokenPara(UsuarioAutenticado autenticado) {
        return responder(usuarioService.findEntityById(autenticado.id()));
    }

    /** O cidadão cria a própria conta e já recebe o token; gestor só é criado por outro gestor. */
    @Transactional
    public LoginResponse registrar(UsuarioRequest request) {
        return responder(usuarioService.criar(request, Role.CIDADAO));
    }

    /** Troca a senha de quem está logado, conferindo a senha atual. */
    @Transactional
    public void trocarSenha(TrocaSenhaRequest request) {
        Usuario usuario = usuarioService.findEntityById(CurrentUser.requireUsuarioId());
        if (!passwordEncoder.matches(request.senhaAtual(), usuario.getSenha())) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Senha atual incorreta.");
        }
        usuario.setSenha(passwordEncoder.encode(request.novaSenha()));
        repository.save(usuario);
    }

    private LoginResponse responder(Usuario usuario) {
        TokenService.Token token = tokenService.gerar(usuario);
        return new LoginResponse(token.valor(), "Bearer", token.expiraEm().toString(), usuario.getPerfil(), usuario.getId());
    }
}
