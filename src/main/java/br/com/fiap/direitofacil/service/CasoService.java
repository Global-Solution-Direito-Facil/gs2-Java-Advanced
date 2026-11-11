package br.com.fiap.direitofacil.service;

import br.com.fiap.direitofacil.auth.CurrentUser;
import br.com.fiap.direitofacil.dto.request.AtualizarStatusCasoRequest;
import br.com.fiap.direitofacil.dto.response.CasoResponse;
import br.com.fiap.direitofacil.dto.response.HistoricoCasoResponse;
import br.com.fiap.direitofacil.entity.Caso;
import br.com.fiap.direitofacil.enums.ResultadoElegibilidade;
import br.com.fiap.direitofacil.enums.StatusCaso;
import br.com.fiap.direitofacil.repository.AuditoriaCasoRepository;
import br.com.fiap.direitofacil.repository.CasoRepository;
import br.com.fiap.direitofacil.repository.PlSqlRepository;
import br.com.fiap.direitofacil.specification.CasoSpecification;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CasoService {

    private final CasoRepository repository;
    private final PlSqlRepository plSqlRepository;
    private final EntityManager entityManager;
    private final AuditoriaCasoRepository auditoriaRepository;

    @Transactional(readOnly = true)
    public Page<CasoResponse> listar(Long pessoaId, StatusCaso status, ResultadoElegibilidade resultado, Pageable pageable) {
        Long usuarioFiltro = CurrentUser.isGestor() ? null : CurrentUser.requireUsuarioId();
        return repository.findAll(CasoSpecification.filtrar(usuarioFiltro, pessoaId, status, resultado), pageable)
                .map(DtoMapper::toResponse);
    }

    /** Lista "meus pedidos" do app: os casos de todos os beneficiários do cidadão logado. */
    @Transactional(readOnly = true)
    public Page<CasoResponse> meusCasos(Pageable pageable) {
        if (!CurrentUser.isCidadao()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Apenas cidadãos possuem a lista de meus casos.");
        }
        return repository.findByPessoaUsuarioId(CurrentUser.requireUsuarioId(), pageable).map(DtoMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public CasoResponse buscarPorId(Long id) {
        return DtoMapper.toResponse(findEntityByIdAutorizado(id));
    }

    /** Gestor muda o status pelo PKG_CASOS, para a trigger validar o fluxo e gravar a auditoria. */
    @Transactional
    public CasoResponse atualizarStatus(Long id, AtualizarStatusCasoRequest request) {
        CurrentUser.requireGestor();
        Caso caso = findEntityById(id);
        repository.flush();
        plSqlRepository.atualizarStatusCaso(id, request.status().name(), LocalDateTime.now());
        entityManager.refresh(caso);
        return DtoMapper.toResponse(caso);
    }

    /** Histórico do caso lido da trilha de auditoria que o gatilho do banco grava a cada mudança. */
    @Transactional(readOnly = true)
    public List<HistoricoCasoResponse> historico(Long id) {
        findEntityByIdAutorizado(id);
        return auditoriaRepository.findByCasoIdOrderByIdAsc(id).stream()
                .map(DtoMapper::toResponse)
                .toList();
    }

    /** Carrega a entidade ou lança 404. */
    @Transactional(readOnly = true)
    public Caso findEntityById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Caso não encontrado: " + id));
    }

    /** Igual ao findEntityById, mas quem não é gestor só acessa casos dos próprios beneficiários. */
    @Transactional(readOnly = true)
    public Caso findEntityByIdAutorizado(Long id) {
        Caso caso = findEntityById(id);
        if (!CurrentUser.isGestor() && !caso.getPessoa().getUsuario().getId().equals(CurrentUser.requireUsuarioId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você só pode acessar os próprios casos.");
        }
        return caso;
    }
}
