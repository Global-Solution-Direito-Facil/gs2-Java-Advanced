package br.com.fiap.diretofacil.service;

import br.com.fiap.diretofacil.auth.CurrentUser;
import br.com.fiap.diretofacil.dto.request.LaudoRequest;
import br.com.fiap.diretofacil.dto.response.LaudoResponse;
import br.com.fiap.diretofacil.entity.Documento;
import br.com.fiap.diretofacil.entity.Laudo;
import br.com.fiap.diretofacil.entity.Pessoa;
import br.com.fiap.diretofacil.repository.LaudoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LaudoService {

    private final LaudoRepository repository;
    private final PessoaService pessoaService;
    private final DocumentoService documentoService;
    private final ElegibilidadeService elegibilidadeService;

    @Transactional(readOnly = true)
    public List<LaudoResponse> listarPorPessoa(Long pessoaId) {
        pessoaService.findEntityByIdAutorizado(pessoaId);
        return repository.findByPessoaIdOrderByDataLaudoDesc(pessoaId).stream()
                .map(DtoMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public LaudoResponse buscarPorId(Long id) {
        Laudo laudo = findEntityById(id);
        pessoaService.findEntityByIdAutorizado(laudo.getPessoa().getId());
        return DtoMapper.toResponse(laudo);
    }

    /**
     * Grava o laudo estruturado (só gestor ou a extração da IA) e dispara a regra PL/SQL,
     * que abre os casos ELEGIVEL e POSSIVEL da pessoa. Reprocessar o mesmo documento devolve o laudo já existente.
     */
    @Transactional
    public LaudoResponse registrar(LaudoRequest request) {
        CurrentUser.requireGestor();
        Pessoa pessoa = pessoaService.findEntityById(request.pessoaId());

        Documento documento = null;
        if (request.documentoId() != null) {
            documento = documentoService.findEntityById(request.documentoId());
            if (!documento.getPessoa().getId().equals(pessoa.getId())) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "O documento pertence a outra pessoa.");
            }
            var jaExtraido = repository.findByDocumentoId(documento.getId());
            if (jaExtraido.isPresent()) {
                return DtoMapper.toResponse(jaExtraido.get());
            }
        }

        Laudo laudo = repository.saveAndFlush(Laudo.builder()
                .pessoa(pessoa)
                .documento(documento)
                .codCid(request.codCid())
                .condicao(request.condicao())
                .tipoCondicao(request.tipoCondicao())
                .dataLaudo(request.dataLaudo())
                .build());

        elegibilidadeService.abrirCasos(pessoa.getId());
        if (documento != null) {
            documentoService.marcarProcessado(documento.getId());
        }
        return DtoMapper.toResponse(laudo);
    }

    /** Carrega a entidade ou lança 404. */
    @Transactional(readOnly = true)
    public Laudo findEntityById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Laudo não encontrado: " + id));
    }
}
