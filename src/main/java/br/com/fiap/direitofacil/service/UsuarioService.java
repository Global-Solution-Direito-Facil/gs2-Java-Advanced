package br.com.fiap.direitofacil.service;

import br.com.fiap.direitofacil.auth.CurrentUser;
import br.com.fiap.direitofacil.dto.request.UsuarioRequest;
import br.com.fiap.direitofacil.dto.request.UsuarioUpdateRequest;
import br.com.fiap.direitofacil.dto.response.UsuarioResponse;
import br.com.fiap.direitofacil.entity.Usuario;
import br.com.fiap.direitofacil.enums.Role;
import br.com.fiap.direitofacil.repository.UsuarioRepository;
import br.com.fiap.direitofacil.validation.CpfValidator;
import br.com.fiap.direitofacil.validation.EmailValidoValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;

    /** Equipe de gestores; só gestor enxerga. */
    @Transactional(readOnly = true)
    public Page<UsuarioResponse> listarGestores(Pageable pageable) {
        CurrentUser.requireGestor();
        return repository.findByPerfil(Role.GESTOR, pageable).map(DtoMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public UsuarioResponse buscarPorId(Long id) {
        return DtoMapper.toResponse(findEntityByIdAutorizado(id));
    }

    /** Devolve a conta de quem está logado, lendo o id do token. */
    @Transactional(readOnly = true)
    public UsuarioResponse meuPerfil() {
        return DtoMapper.toResponse(findEntityById(CurrentUser.requireUsuarioId()));
    }

    /** Cria um gestor a partir de um gestor logado; o cidadão se registra pelo AuthService. */
    @Transactional
    public UsuarioResponse criarGestor(UsuarioRequest request) {
        CurrentUser.requireGestor();
        return DtoMapper.toResponse(criar(request, Role.GESTOR));
    }

    /** Cria a conta com CPF normalizado e senha em BCrypt, recusando email ou CPF já cadastrados. */
    @Transactional
    public Usuario criar(UsuarioRequest request, Role perfil) {
        String email = EmailValidoValidator.normalizar(request.email());
        String cpf = CpfValidator.normalizar(request.cpf());
        if (repository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email já cadastrado.");
        }
        if (repository.existsByCpf(cpf)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "CPF já cadastrado.");
        }
        return repository.save(Usuario.builder()
                .nome(request.nome())
                .cpf(cpf)
                .email(email)
                .senha(passwordEncoder.encode(request.senha()))
                .perfil(perfil)
                .build());
    }

    @Transactional
    public UsuarioResponse atualizar(Long id, UsuarioUpdateRequest request) {
        Usuario usuario = findEntityByIdAutorizado(id);
        usuario.setNome(request.nome());
        return DtoMapper.toResponse(repository.save(usuario));
    }

    /** Carrega a conta ou lança 404; usado pelo AuthService e pelo PessoaService. */
    @Transactional(readOnly = true)
    public Usuario findEntityById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado: " + id));
    }

    /** Igual ao findEntityById, mas quem não é gestor só acessa a própria conta. */
    @Transactional(readOnly = true)
    public Usuario findEntityByIdAutorizado(Long id) {
        Usuario usuario = findEntityById(id);
        if (!CurrentUser.isGestor() && !usuario.getId().equals(CurrentUser.requireUsuarioId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você só pode acessar a própria conta.");
        }
        return usuario;
    }
}
