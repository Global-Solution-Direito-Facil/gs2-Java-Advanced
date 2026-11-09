package br.com.fiap.diretofacil.service;

import br.com.fiap.diretofacil.auth.CurrentUser;
import br.com.fiap.diretofacil.dto.response.AvaliacaoDireitoResponse;
import br.com.fiap.diretofacil.dto.response.DocumentoResponse;
import br.com.fiap.diretofacil.entity.Documento;
import br.com.fiap.diretofacil.repository.AvaliacaoDireitoRepository;
import br.com.fiap.diretofacil.repository.DocumentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentoService {

    private final DocumentoRepository repository;
    private final AvaliacaoDireitoRepository avaliacaoRepository;

    @Transactional(readOnly = true)
    public Page<DocumentoResponse> listar(Long cidadaoId, Pageable pageable) {
        if (CurrentUser.isCidadao()) {
            return repository.findByCidadaoId(CurrentUser.perfilId(), pageable).map(DtoMapper::toResponse);
        }
        Page<Documento> pagina = cidadaoId == null
                ? repository.findAll(pageable)
                : repository.findByCidadaoId(cidadaoId, pageable);
        return pagina.map(DtoMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public DocumentoResponse buscarPorId(Long id) {
        return DtoMapper.toResponse(findEntityByIdAutorizado(id));
    }

    /** Resultado da elegibilidade de um laudo, todos os direitos com elegíveis primeiro. */
    @Transactional(readOnly = true)
    public List<AvaliacaoDireitoResponse> avaliacoesDoDocumento(Long id) {
        findEntityByIdAutorizado(id);
        return avaliacaoRepository.findByDocumentoIdOrderByResultadoAsc(id).stream()
                .map(DtoMapper::toResponse)
                .toList();
    }

    /** Carrega a entidade ou lança 404; usado pelo listener da fila. */
    @Transactional(readOnly = true)
    public Documento findEntityById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Documento não encontrado: " + id));
    }

    /** Igual ao findEntityById, mas garante que um cidadão só acessa os próprios documentos. */
    @Transactional(readOnly = true)
    public Documento findEntityByIdAutorizado(Long id) {
        Documento documento = findEntityById(id);
        if (CurrentUser.isCidadao() && !documento.getCidadao().getId().equals(CurrentUser.perfilId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você só pode acessar os próprios documentos.");
        }
        return documento;
    }
}
