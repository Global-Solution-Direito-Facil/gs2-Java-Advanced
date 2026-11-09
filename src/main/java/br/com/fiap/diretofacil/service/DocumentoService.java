package br.com.fiap.diretofacil.service;

import br.com.fiap.diretofacil.auth.CurrentUser;
import br.com.fiap.diretofacil.dto.request.DocumentoRequest;
import br.com.fiap.diretofacil.dto.response.DocumentoResponse;
import br.com.fiap.diretofacil.entity.Documento;
import br.com.fiap.diretofacil.entity.Pessoa;
import br.com.fiap.diretofacil.enums.StatusDocumento;
import br.com.fiap.diretofacil.repository.DocumentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DocumentoService {

    private final DocumentoRepository repository;
    private final PessoaService pessoaService;

    @Transactional(readOnly = true)
    public Page<DocumentoResponse> listar(Long pessoaId, Pageable pageable) {
        if (!CurrentUser.isGestor()) {
            return repository.findByPessoaUsuarioId(CurrentUser.requireUsuarioId(), pageable).map(DtoMapper::toResponse);
        }
        Page<Documento> pagina = pessoaId == null
                ? repository.findAll(pageable)
                : repository.findByPessoaId(pessoaId, pageable);
        return pagina.map(DtoMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public DocumentoResponse buscarPorId(Long id) {
        return DtoMapper.toResponse(findEntityByIdAutorizado(id));
    }

    /** Registra o documento anunciado; reenvio com a mesma chave devolve o registro já salvo, desde que da mesma pessoa. */
    @Transactional
    public DocumentoResponse registrar(DocumentoRequest request) {
        Pessoa pessoa = pessoaService.findEntityByIdAutorizado(request.pessoaId());

        Optional<Documento> existente = repository.findByChaveIdempotencia(request.chaveIdempotencia());
        if (existente.isPresent()) {
            return DtoMapper.toResponse(conferirDono(existente.get(), pessoa));
        }

        Documento documento = Documento.builder()
                .pessoa(pessoa)
                .idExterno(request.idExterno())
                .nomeArquivo(request.nomeArquivo())
                .tipoMidia(request.tipoMidia())
                .tamanhoBytes(request.tamanhoBytes())
                .chaveIdempotencia(request.chaveIdempotencia())
                .build();
        try {
            return DtoMapper.toResponse(repository.saveAndFlush(documento));
        } catch (DataIntegrityViolationException e) {
            Documento concorrente = repository.findByChaveIdempotencia(request.chaveIdempotencia())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Documento já registrado."));
            return DtoMapper.toResponse(conferirDono(concorrente, pessoa));
        }
    }

    /** Marca o documento como PROCESSADO depois da extração do laudo. */
    @Transactional
    public void marcarProcessado(Long id) {
        Documento documento = findEntityById(id);
        documento.setStatus(StatusDocumento.PROCESSADO);
        documento.setDataProcessamento(LocalDateTime.now());
        repository.save(documento);
    }

    /** Marca o documento como ERRO quando a extração falha. */
    @Transactional
    public void marcarErro(Long id) {
        Documento documento = findEntityById(id);
        documento.setStatus(StatusDocumento.ERRO);
        documento.setDataProcessamento(LocalDateTime.now());
        repository.save(documento);
    }

    /** Carrega a entidade ou lança 404; usado pelo listener da fila. */
    @Transactional(readOnly = true)
    public Documento findEntityById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Documento não encontrado: " + id));
    }

    /** Igual ao findEntityById, mas quem não é gestor só acessa documentos dos próprios beneficiários. */
    @Transactional(readOnly = true)
    public Documento findEntityByIdAutorizado(Long id) {
        Documento documento = findEntityById(id);
        if (!CurrentUser.isGestor() && !documento.getPessoa().getUsuario().getId().equals(CurrentUser.requireUsuarioId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você só pode acessar os próprios documentos.");
        }
        return documento;
    }

    /** Garante que um documento já registrado com a mesma chave pertence à pessoa informada. */
    private Documento conferirDono(Documento documento, Pessoa pessoa) {
        if (!documento.getPessoa().getId().equals(pessoa.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Chave de idempotência já usada por outro documento.");
        }
        return documento;
    }
}
