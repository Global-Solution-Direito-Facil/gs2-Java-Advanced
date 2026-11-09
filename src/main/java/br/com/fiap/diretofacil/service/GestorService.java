package br.com.fiap.diretofacil.service;

import br.com.fiap.diretofacil.auth.CurrentUser;
import br.com.fiap.diretofacil.dto.request.GestorRequest;
import br.com.fiap.diretofacil.dto.request.GestorUpdateRequest;
import br.com.fiap.diretofacil.dto.response.GestorResponse;
import br.com.fiap.diretofacil.entity.Gestor;
import br.com.fiap.diretofacil.entity.Usuario;
import br.com.fiap.diretofacil.enums.Role;
import br.com.fiap.diretofacil.repository.GestorRepository;
import br.com.fiap.diretofacil.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class GestorService {

    private final GestorRepository repository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public Page<GestorResponse> listar(Pageable pageable) {
        return repository.findByAtivoTrue(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public GestorResponse buscarPorId(Long id) {
        return toResponse(findEntityById(id));
    }

    /** Devolve o perfil do gestor logado, lendo o id do token. */
    @Transactional(readOnly = true)
    public GestorResponse meuPerfil() {
        if (!CurrentUser.isGestor()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Apenas gestores possuem perfil de gestor.");
        }
        return toResponse(findEntityById(CurrentUser.perfilId()));
    }

    @Transactional
    public GestorResponse criar(GestorRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email já cadastrado.");
        }
        if (repository.existsByCpf(request.cpf())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "CPF já cadastrado.");
        }
        if (repository.existsByMatricula(request.matricula())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Matrícula já cadastrada.");
        }

        Gestor gestor = repository.save(Gestor.builder()
                .nome(request.nome())
                .cpf(request.cpf())
                .matricula(request.matricula())
                .unidade(request.unidade())
                .telefone(request.telefone())
                .build());

        usuarioRepository.save(Usuario.builder()
                .email(email)
                .senhaHash(passwordEncoder.encode(request.senha()))
                .role(Role.GESTOR)
                .gestor(gestor)
                .build());

        return DtoMapper.toResponse(gestor, email);
    }

    @Transactional
    public GestorResponse atualizar(Long id, GestorUpdateRequest request) {
        Gestor gestor = findEntityById(id);
        gestor.setNome(request.nome());
        gestor.setUnidade(request.unidade());
        gestor.setTelefone(request.telefone());
        return toResponse(repository.save(gestor));
    }

    /** Desliga o gestor e a conta dele sem apagar nada; recusa se for a própria conta. */
    @Transactional
    public void desativar(Long id) {
        if (id.equals(CurrentUser.perfilId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Um gestor não pode desativar a própria conta.");
        }
        Gestor gestor = findEntityById(id);
        gestor.setAtivo(false);
        repository.save(gestor);
        usuarioRepository.findByGestorId(id).ifPresent(usuario -> {
            usuario.setAtivo(false);
            usuarioRepository.save(usuario);
        });
    }

    /** Carrega a entidade ou lança 404; usado pelo CasoService ao atribuir o responsável. */
    @Transactional(readOnly = true)
    public Gestor findEntityById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Gestor não encontrado: " + id));
    }

    /** Monta o response buscando o email na conta, já que ele não fica na entidade de perfil. */
    private GestorResponse toResponse(Gestor gestor) {
        String email = usuarioRepository.findByGestorId(gestor.getId())
                .map(Usuario::getEmail)
                .orElse(null);
        return DtoMapper.toResponse(gestor, email);
    }
}
