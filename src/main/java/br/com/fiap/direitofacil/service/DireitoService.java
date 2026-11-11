package br.com.fiap.direitofacil.service;

import br.com.fiap.direitofacil.auth.CurrentUser;
import br.com.fiap.direitofacil.dto.request.CriterioDireitoRequest;
import br.com.fiap.direitofacil.dto.request.DireitoRequest;
import br.com.fiap.direitofacil.dto.response.DireitoResponse;
import br.com.fiap.direitofacil.entity.CriterioDireito;
import br.com.fiap.direitofacil.entity.Direito;
import br.com.fiap.direitofacil.enums.GrupoAlvo;
import br.com.fiap.direitofacil.repository.CasoRepository;
import br.com.fiap.direitofacil.repository.CriterioDireitoRepository;
import br.com.fiap.direitofacil.repository.DireitoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class DireitoService {

    private final DireitoRepository repository;
    private final CriterioDireitoRepository criterioRepository;
    private final CasoRepository casoRepository;

    @Transactional(readOnly = true)
    public Page<DireitoResponse> listar(GrupoAlvo grupoAlvo, Pageable pageable) {
        Page<Direito> pagina = grupoAlvo == null
                ? repository.findAll(pageable)
                : repository.findByGrupoAlvo(grupoAlvo, pageable);
        return pagina.map(DtoMapper::toResponse);
    }

    /** Catálogo inteiro, na ordem do id, que é a mesma que a PRC_ABRIR_CASOS percorre. */
    @Transactional(readOnly = true)
    public List<Direito> listarTodos() {
        return repository.findAll(Sort.by("id"));
    }

    @Transactional(readOnly = true)
    public DireitoResponse buscarPorId(Long id) {
        return DtoMapper.toResponse(findEntityById(id));
    }

    /** Busca pelo código estável (BPC_PCD, CIPTEA...), que é como o app e o .NET referenciam um direito. */
    @Transactional(readOnly = true)
    public DireitoResponse buscarPorCodigo(String codigo) {
        return repository.findByCodigo(codigo.toUpperCase(Locale.ROOT))
                .map(DtoMapper::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Direito não encontrado: " + codigo));
    }

    /** Só gestor mantém o catálogo. */
    @Transactional
    public DireitoResponse criar(DireitoRequest request) {
        CurrentUser.requireGestor();
        String codigo = request.codigo().toUpperCase(Locale.ROOT);
        if (repository.existsByCodigo(codigo)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Código já cadastrado.");
        }
        Direito direito = new Direito();
        aplicar(direito, request, codigo);
        direito = repository.save(direito);

        CriterioDireito criterio = CriterioDireito.builder().direito(direito).build();
        aplicar(criterio, request.criterio());
        direito.setCriterio(criterioRepository.save(criterio));
        return DtoMapper.toResponse(direito);
    }

    /** Só gestor mantém o catálogo. */
    @Transactional
    public DireitoResponse atualizar(Long id, DireitoRequest request) {
        CurrentUser.requireGestor();
        Direito direito = findEntityById(id);
        String codigo = request.codigo().toUpperCase(Locale.ROOT);
        if (!direito.getCodigo().equals(codigo) && repository.existsByCodigo(codigo)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Código já cadastrado.");
        }
        aplicar(direito, request, codigo);
        direito = repository.save(direito);

        Direito salvo = direito;
        CriterioDireito criterio = criterioRepository.findByDireitoId(id)
                .orElseGet(() -> CriterioDireito.builder().direito(salvo).build());
        aplicar(criterio, request.criterio());
        direito.setCriterio(criterioRepository.save(criterio));
        return DtoMapper.toResponse(direito);
    }

    /** Só gestor; remove o direito e o critério, e recusa com 409 se já houver casos ligados a ele. */
    @Transactional
    public void deletar(Long id) {
        CurrentUser.requireGestor();
        Direito direito = findEntityById(id);
        if (casoRepository.existsByDireitoId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Direito possui casos abertos e não pode ser removido.");
        }
        criterioRepository.findByDireitoId(id).ifPresent(criterioRepository::delete);
        repository.delete(direito);
    }

    /** Carrega a entidade ou lança 404. */
    @Transactional(readOnly = true)
    public Direito findEntityById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Direito não encontrado: " + id));
    }

    /** Copia os campos do request para o direito; compartilhado por criar e atualizar. */
    private void aplicar(Direito direito, DireitoRequest request, String codigo) {
        direito.setCodigo(codigo);
        direito.setNome(request.nome());
        direito.setOrgao(request.orgao());
        direito.setBaseLegal(request.baseLegal());
        direito.setGrupoAlvo(request.grupoAlvo());
        direito.setPrazoDias(request.prazoDias());
        direito.setStatusVerificacao(request.statusVerificacao());
    }

    /** Copia os campos do request para o critério; compartilhado por criar e atualizar. */
    private void aplicar(CriterioDireito criterio, CriterioDireitoRequest request) {
        criterio.setIdadeMin(request.idadeMin());
        criterio.setIdadeMax(request.idadeMax());
        criterio.setExigePcd(request.exigePcd());
        criterio.setExigeTea(request.exigeTea());
        criterio.setFracaoSmMax(request.fracaoSmMax());
        criterio.setPctTolerancia(request.pctTolerancia());
    }
}
