package br.com.fiap.direitofacil.service;

import br.com.fiap.direitofacil.auth.CurrentUser;
import br.com.fiap.direitofacil.dto.request.ReceitaItemRequest;
import br.com.fiap.direitofacil.dto.request.ReceitaRequest;
import br.com.fiap.direitofacil.dto.response.ReceitaItemResponse;
import br.com.fiap.direitofacil.dto.response.ReceitaResponse;
import br.com.fiap.direitofacil.entity.Documento;
import br.com.fiap.direitofacil.entity.Pessoa;
import br.com.fiap.direitofacil.entity.Receita;
import br.com.fiap.direitofacil.repository.PlSqlRepository;
import br.com.fiap.direitofacil.repository.ReceitaItemRepository;
import br.com.fiap.direitofacil.repository.ReceitaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Receitas do Farmácia Popular. A IA lê a foto, a pessoa confere na tela e só então o app grava
 * cada remédio; a PKG_FARMACIA_POPULAR compara com o elenco e decide o resultado e a orientação.
 */
@Service
@RequiredArgsConstructor
public class ReceitaService {

    private final ReceitaRepository repository;
    private final ReceitaItemRepository itemRepository;
    private final PessoaService pessoaService;
    private final DocumentoService documentoService;
    private final PlSqlRepository plSqlRepository;

    /** Cidadão vê as receitas dos próprios beneficiários; gestor vê todas ou filtra por pessoa. */
    @Transactional(readOnly = true)
    public Page<ReceitaResponse> listar(Long pessoaId, Pageable pageable) {
        Page<Receita> pagina;
        if (!CurrentUser.isGestor()) {
            pagina = repository.findByPessoaUsuarioId(CurrentUser.requireUsuarioId(), pageable);
        } else {
            pagina = pessoaId == null ? repository.findAll(pageable) : repository.findByPessoaId(pessoaId, pageable);
        }
        return pagina.map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public ReceitaResponse buscarPorId(Long id) {
        return toResponse(findEntityByIdAutorizado(id));
    }

    /** Abre a receita pela PRC_REGISTRAR_RECEITA; o documento, se informado, precisa ser da mesma pessoa. */
    @Transactional
    public ReceitaResponse criar(ReceitaRequest request) {
        Pessoa pessoa = pessoaService.findEntityByIdAutorizado(request.pessoaId());
        if (request.documentoId() != null) {
            Documento documento = documentoService.findEntityById(request.documentoId());
            if (!documento.getPessoa().getId().equals(pessoa.getId())) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "O documento pertence a outra pessoa.");
            }
        }
        Long id = plSqlRepository.registrarReceita(pessoa.getId(), request.documentoId());
        return toResponse(findEntityById(id));
    }

    /** Grava um remédio já conferido pela PRC_ADICIONAR_ITEM, que decide o resultado pelo elenco. */
    @Transactional
    public ReceitaItemResponse adicionarItem(Long receitaId, ReceitaItemRequest request) {
        findEntityByIdAutorizado(receitaId);
        plSqlRepository.adicionarItemReceita(receitaId, request.nomeLido(), request.principioAtivo(),
                request.concentracao(), Boolean.TRUE.equals(request.corrigido()));
        return itemRepository.findTopByReceitaIdOrderByIdDesc(receitaId)
                .map(DtoMapper::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Item não foi gravado."));
    }

    /** Remove a receita e os itens; dono ou gestor. */
    @Transactional
    public void deletar(Long id) {
        Receita receita = findEntityByIdAutorizado(id);
        itemRepository.deleteByReceitaId(receita.getId());
        itemRepository.flush();
        repository.delete(receita);
    }

    @Transactional(readOnly = true)
    public Receita findEntityById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Receita não encontrada: " + id));
    }

    /** Quem não é gestor só acessa as receitas dos próprios beneficiários. */
    @Transactional(readOnly = true)
    public Receita findEntityByIdAutorizado(Long id) {
        Receita receita = findEntityById(id);
        if (!CurrentUser.isGestor() && !receita.getPessoa().getUsuario().getId().equals(CurrentUser.requireUsuarioId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você só pode acessar as próprias receitas.");
        }
        return receita;
    }

    private ReceitaResponse toResponse(Receita receita) {
        return DtoMapper.toResponse(receita, itemRepository.findByReceitaIdOrderByIdAsc(receita.getId()));
    }
}
