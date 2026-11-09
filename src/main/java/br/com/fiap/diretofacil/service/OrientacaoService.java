package br.com.fiap.diretofacil.service;

import br.com.fiap.diretofacil.auth.CurrentUser;
import br.com.fiap.diretofacil.dto.request.RegistrarOrientacaoRequest;
import br.com.fiap.diretofacil.dto.response.OrientacaoResponse;
import br.com.fiap.diretofacil.entity.Orientacao;
import br.com.fiap.diretofacil.repository.OrientacaoRepository;
import br.com.fiap.diretofacil.repository.PlSqlRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class OrientacaoService {

    private final OrientacaoRepository repository;
    private final PlSqlRepository plSqlRepository;
    private final CasoService casoService;
    private final EntityManager entityManager;

    @Transactional(readOnly = true)
    public OrientacaoResponse buscarPorCaso(Long casoId) {
        casoService.findEntityByIdAutorizado(casoId);
        return repository.findByCasoId(casoId)
                .map(DtoMapper::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Caso ainda sem orientação: " + casoId));
    }

    /** Grava ou substitui o passo a passo via PKG_CASOS; só gestor ou o RAG, e relê a linha depois do MERGE. */
    @Transactional
    public OrientacaoResponse registrar(Long casoId, RegistrarOrientacaoRequest request) {
        CurrentUser.requireGestor();
        casoService.findEntityById(casoId);
        entityManager.flush();
        entityManager.clear();
        plSqlRepository.registrarOrientacao(casoId, request.passoAPasso(), request.fonte(), request.modelo());
        Orientacao orientacao = repository.findByCasoId(casoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Orientação não foi gravada."));
        entityManager.refresh(orientacao);
        return DtoMapper.toResponse(orientacao);
    }
}
