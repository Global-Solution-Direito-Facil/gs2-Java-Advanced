package br.com.fiap.diretofacil.service;

import br.com.fiap.diretofacil.auth.CurrentUser;
import br.com.fiap.diretofacil.dto.request.AtualizarStatusCasoRequest;
import br.com.fiap.diretofacil.dto.response.CasoResponse;
import br.com.fiap.diretofacil.entity.Caso;
import br.com.fiap.diretofacil.enums.ResultadoElegibilidade;
import br.com.fiap.diretofacil.enums.StatusCaso;
import br.com.fiap.diretofacil.repository.CasoRepository;
import br.com.fiap.diretofacil.specification.CasoSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class CasoService {

    private final CasoRepository repository;
    private final GestorService gestorService;

    @Transactional(readOnly = true)
    public Page<CasoResponse> listar(Long cidadaoId, StatusCaso status, ResultadoElegibilidade resultado,
                                     Long gestorId, Pageable pageable) {
        Long cidadaoFiltro = CurrentUser.isCidadao() ? CurrentUser.perfilId() : cidadaoId;
        return repository.findAll(CasoSpecification.filtrar(cidadaoFiltro, status, resultado, gestorId), pageable)
                .map(DtoMapper::toResponse);
    }

    /** Lista "meus pedidos" do app: os casos do cidadão logado. */
    @Transactional(readOnly = true)
    public Page<CasoResponse> meusCasos(Pageable pageable) {
        if (!CurrentUser.isCidadao()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Apenas cidadãos possuem a lista de meus casos.");
        }
        return repository.findByCidadaoId(CurrentUser.perfilId(), pageable).map(DtoMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public CasoResponse buscarPorId(Long id) {
        return DtoMapper.toResponse(findEntityByIdAutorizado(id));
    }

    /** Gestor movimenta o status, observação e prazo; se o caso não tinha responsável, ele assume. */
    @Transactional
    public CasoResponse atualizarStatus(Long id, AtualizarStatusCasoRequest request) {
        Caso caso = findEntityById(id);
        caso.setStatus(request.status());
        if (request.observacaoGestor() != null) {
            caso.setObservacaoGestor(request.observacaoGestor());
        }
        if (request.prazo() != null) {
            caso.setPrazo(request.prazo());
        }
        if (caso.getGestorResponsavel() == null) {
            caso.setGestorResponsavel(gestorService.findEntityById(CurrentUser.perfilId()));
        }
        return DtoMapper.toResponse(repository.save(caso));
    }

    /** Gestor logado vira o responsável pelo caso; se estava ABERTO, passa a EM_ANDAMENTO. */
    @Transactional
    public CasoResponse assumir(Long id) {
        Caso caso = findEntityById(id);
        caso.setGestorResponsavel(gestorService.findEntityById(CurrentUser.perfilId()));
        if (caso.getStatus() == StatusCaso.ABERTO) {
            caso.setStatus(StatusCaso.EM_ANDAMENTO);
        }
        return DtoMapper.toResponse(repository.save(caso));
    }

    /** Carrega a entidade ou lança 404. */
    @Transactional(readOnly = true)
    public Caso findEntityById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Caso não encontrado: " + id));
    }

    /** Igual ao findEntityById, mas garante que um cidadão só acessa os próprios casos. */
    @Transactional(readOnly = true)
    public Caso findEntityByIdAutorizado(Long id) {
        Caso caso = findEntityById(id);
        if (CurrentUser.isCidadao() && !caso.getCidadao().getId().equals(CurrentUser.perfilId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você só pode acessar os próprios casos.");
        }
        return caso;
    }
}
