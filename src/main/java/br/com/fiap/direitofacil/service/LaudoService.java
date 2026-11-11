package br.com.fiap.direitofacil.service;

import br.com.fiap.direitofacil.auth.CurrentUser;
import br.com.fiap.direitofacil.dto.request.LaudoRequest;
import br.com.fiap.direitofacil.dto.response.LaudoResponse;
import br.com.fiap.direitofacil.entity.Documento;
import br.com.fiap.direitofacil.entity.Laudo;
import br.com.fiap.direitofacil.entity.Pessoa;
import br.com.fiap.direitofacil.repository.LaudoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class LaudoService {

    private final LaudoRepository repository;
    private final PessoaService pessoaService;
    private final DocumentoService documentoService;
    private final ElegibilidadeService elegibilidadeService;
    private final CasoEventoService casoEventoService;

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
     * Grava o laudo estruturado (só gestor ou a extração da IA) e dispara a reavaliação PL/SQL,
     * que atualiza os casos ABERTO e abre os ELEGIVEL e POSSIVEL da pessoa. Reprocessar o mesmo
     * documento devolve o laudo já existente.
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

        Set<Long> casosAntes = casoEventoService.idsDosCasos(pessoa.getId());
        Laudo laudo = repository.saveAndFlush(Laudo.builder()
                .pessoa(pessoa)
                .documento(documento)
                .codCid(request.codCid())
                .condicao(request.condicao())
                .tipoCondicao(request.tipoCondicao())
                .dataLaudo(request.dataLaudo())
                .build());

        elegibilidadeService.reavaliarCasos(pessoa.getId());
        casoEventoService.publicarNovos(pessoa.getId(), casosAntes);
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
