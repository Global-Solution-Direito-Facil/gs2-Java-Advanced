package br.com.fiap.diretofacil.service;

import br.com.fiap.diretofacil.dto.request.DireitoRequest;
import br.com.fiap.diretofacil.dto.response.DireitoResponse;
import br.com.fiap.diretofacil.entity.Direito;
import br.com.fiap.diretofacil.enums.Publico;
import br.com.fiap.diretofacil.repository.DireitoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DireitoService {

    private final DireitoRepository repository;

    @Transactional(readOnly = true)
    public Page<DireitoResponse> listar(Publico publico, Pageable pageable) {
        Page<Direito> pagina = publico == null
                ? repository.findByAtivoTrue(pageable)
                : repository.findByAtivoTrueAndPublicosContaining(publico, pageable);
        return pagina.map(DtoMapper::toResponse);
    }

    /** Lista de direitos que a avaliação de elegibilidade percorre para cada laudo. */
    @Transactional(readOnly = true)
    public List<Direito> listarAtivos() {
        return repository.findByAtivoTrue();
    }

    @Transactional(readOnly = true)
    public DireitoResponse buscarPorId(Long id) {
        return DtoMapper.toResponse(findEntityById(id));
    }

    /** Busca pelo código estável (BPC_PCD, CIPTEA...), que é como o app e o .NET referenciam um direito. */
    @Transactional(readOnly = true)
    public DireitoResponse buscarPorCodigo(String codigo) {
        return repository.findByCodigo(codigo)
                .map(DtoMapper::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Direito não encontrado: " + codigo));
    }

    @Transactional
    public DireitoResponse criar(DireitoRequest request) {
        if (repository.existsByCodigo(request.codigo())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Código já cadastrado.");
        }
        Direito direito = Direito.builder()
                .ativo(request.ativo() != null ? request.ativo() : true)
                .build();
        aplicar(direito, request);
        return DtoMapper.toResponse(repository.save(direito));
    }

    @Transactional
    public DireitoResponse atualizar(Long id, DireitoRequest request) {
        Direito direito = findEntityById(id);
        if (!direito.getCodigo().equals(request.codigo()) && repository.existsByCodigo(request.codigo())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Código já cadastrado.");
        }
        aplicar(direito, request);
        if (request.ativo() != null) {
            direito.setAtivo(request.ativo());
        }
        return DtoMapper.toResponse(repository.save(direito));
    }

    /** Tira o direito das avaliações novas sem apagar os casos que já o referenciam. */
    @Transactional
    public void desativar(Long id) {
        Direito direito = findEntityById(id);
        direito.setAtivo(false);
        repository.save(direito);
    }

    /** Carrega a entidade ou lança 404. */
    @Transactional(readOnly = true)
    public Direito findEntityById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Direito não encontrado: " + id));
    }

    /** Copia os campos do request para a entidade; compartilhado por criar e atualizar. */
    private void aplicar(Direito direito, DireitoRequest request) {
        direito.setCodigo(request.codigo());
        direito.setNome(request.nome());
        direito.setCriterios(request.criterios());
        direito.setOndePedir(request.ondePedir());
        direito.setBaseLegal(request.baseLegal());
        direito.setUrlBaseLegal(request.urlBaseLegal());
        direito.setPublicos(new HashSet<>(request.publicos()));
        direito.setRendaPerCapitaMaxSm(request.rendaPerCapitaMaxSm());
        direito.setIdadeMinima(request.idadeMinima());
        direito.setExigeCadunico(request.exigeCadunico());
        direito.setExigeDeficiencia(request.exigeDeficiencia());
        direito.setExigeTea(request.exigeTea());
        direito.setStatusConferencia(request.statusConferencia());
    }
}
