package br.com.fiap.diretofacil.service;

import br.com.fiap.diretofacil.auth.CurrentUser;
import br.com.fiap.diretofacil.dto.request.CidadaoRequest;
import br.com.fiap.diretofacil.dto.response.CidadaoResponse;
import br.com.fiap.diretofacil.entity.Cidadao;
import br.com.fiap.diretofacil.entity.Usuario;
import br.com.fiap.diretofacil.repository.CidadaoRepository;
import br.com.fiap.diretofacil.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;


@Service
@RequiredArgsConstructor
public class CidadaoService {

    private final CidadaoRepository repository;
    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public Page<CidadaoResponse> listar(Pageable pageable) {
        return repository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<CidadaoResponse> buscarPorNome(String nome, Pageable pageable) {
        return repository.findByNomeContainingIgnoreCase(nome, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public CidadaoResponse buscarPorId(Long id) {
        return toResponse(findEntityByIdAutorizado(id));
    }

    /** Devolve o perfil de quem está logado, lendo o id do token em vez de receber por parâmetro. */
    @Transactional(readOnly = true)
    public CidadaoResponse meuPerfil() {
        if (!CurrentUser.isCidadao()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Apenas cidadãos possuem perfil de cidadão.");
        }
        return toResponse(findEntityById(CurrentUser.perfilId()));
    }

    @Transactional
    public CidadaoResponse criar(CidadaoRequest request) {
        if (repository.existsByCpf(request.cpf())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "CPF já cadastrado.");
        }
        Cidadao cidadao = repository.save(Cidadao.builder()
                .nome(request.nome())
                .cpf(request.cpf())
                .dataNascimento(request.dataNascimento())
                .rendaFamiliar(request.rendaFamiliar())
                .qtdPessoasFamilia(request.qtdPessoasFamilia())
                .cadunico(request.cadunico())
                .municipio(request.municipio())
                .uf(request.uf())
                .build());
        return toResponse(cidadao);
    }

    @Transactional
    public CidadaoResponse atualizar(Long id, CidadaoRequest request) {
        Cidadao cidadao = findEntityByIdAutorizado(id);
        if (!cidadao.getCpf().equals(request.cpf()) && repository.existsByCpf(request.cpf())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "CPF já cadastrado.");
        }
        cidadao.setNome(request.nome());
        cidadao.setCpf(request.cpf());
        cidadao.setDataNascimento(request.dataNascimento());
        cidadao.setRendaFamiliar(request.rendaFamiliar());
        cidadao.setQtdPessoasFamilia(request.qtdPessoasFamilia());
        cidadao.setCadunico(request.cadunico());
        cidadao.setMunicipio(request.municipio());
        cidadao.setUf(request.uf());
        return toResponse(repository.save(cidadao));
    }

    /** Carrega a entidade ou lança 404; usado pelos outros services que precisam do Cidadao. */
    @Transactional(readOnly = true)
    public Cidadao findEntityById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cidadão não encontrado: " + id));
    }

    /** Igual ao findEntityById, mas garante que um cidadão só acessa o próprio cadastro. */
    @Transactional(readOnly = true)
    public Cidadao findEntityByIdAutorizado(Long id) {
        Cidadao cidadao = findEntityById(id);
        verificarPermissao(cidadao);
        return cidadao;
    }

    /** Lança 403 se um cidadão tentar acessar o cadastro de outro; gestor passa sempre. */
    private void verificarPermissao(Cidadao cidadao) {
        if (CurrentUser.isCidadao() && !cidadao.getId().equals(CurrentUser.perfilId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você só pode acessar o próprio cadastro.");
        }
    }

    /** Monta o response buscando o email na conta, já que ele não fica na entidade de perfil. */
    private CidadaoResponse toResponse(Cidadao cidadao) {
        String email = usuarioRepository.findByCidadaoId(cidadao.getId())
                .map(Usuario::getEmail)
                .orElse(null);
        return DtoMapper.toResponse(cidadao, email);
    }
}
