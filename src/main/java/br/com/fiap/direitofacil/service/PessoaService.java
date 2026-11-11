package br.com.fiap.direitofacil.service;

import br.com.fiap.direitofacil.auth.CurrentUser;
import br.com.fiap.direitofacil.dto.request.PessoaRequest;
import br.com.fiap.direitofacil.dto.response.PessoaResponse;
import br.com.fiap.direitofacil.entity.Pessoa;
import br.com.fiap.direitofacil.entity.Usuario;
import br.com.fiap.direitofacil.enums.Role;
import br.com.fiap.direitofacil.enums.StatusCaso;
import br.com.fiap.direitofacil.repository.CasoRepository;
import br.com.fiap.direitofacil.repository.DocumentoRepository;
import br.com.fiap.direitofacil.repository.LaudoRepository;
import br.com.fiap.direitofacil.repository.OrientacaoRepository;
import br.com.fiap.direitofacil.repository.PessoaRepository;
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

import java.time.LocalDateTime;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PessoaService {

    private final PessoaRepository repository;
    private final UsuarioService usuarioService;
    private final PlSqlRepository plSqlRepository;
    private final CasoEventoService casoEventoService;
    private final CasoRepository casoRepository;
    private final OrientacaoRepository orientacaoRepository;
    private final LaudoRepository laudoRepository;
    private final DocumentoRepository documentoRepository;
    private final ReceitaRepository receitaRepository;
    private final ReceitaItemRepository receitaItemRepository;

    @Transactional(readOnly = true)
    public Page<PessoaResponse> listar(String nome, Pageable pageable) {
        if (!CurrentUser.isGestor()) {
            return repository.findByUsuarioId(CurrentUser.requireUsuarioId(), pageable).map(DtoMapper::toResponse);
        }
        Page<Pessoa> pagina = nome == null || nome.isBlank()
                ? repository.findAll(pageable)
                : repository.findByNomeContainingIgnoreCase(nome, pageable);
        return pagina.map(DtoMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public PessoaResponse buscarPorId(Long id) {
        return DtoMapper.toResponse(findEntityByIdAutorizado(id));
    }

    /**
     * Cidadão cadastra sob a própria conta; gestor informa o responsável, que precisa ser um CIDADAO.
     * Já avalia o catálogo: direitos só de idade e renda (BPC idoso, tarifa social...) não dependem de laudo.
     */
    @Transactional
    public PessoaResponse criar(PessoaRequest request, Long usuarioId) {
        Long responsavelId = CurrentUser.isGestor() ? usuarioId : CurrentUser.requireUsuarioId();
        if (responsavelId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe o usuário responsável.");
        }
        Usuario responsavel = usuarioService.findEntityById(responsavelId);
        if (responsavel.getPerfil() != Role.CIDADAO) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "O responsável precisa ser um cidadão.");
        }
        Pessoa pessoa = repository.saveAndFlush(Pessoa.builder()
                .usuario(responsavel)
                .nome(request.nome())
                .dataNascimento(request.dataNascimento())
                .rendaFamiliar(request.rendaFamiliar())
                .qtdMembrosFamilia(request.qtdMembrosFamilia())
                .rendaIndividual(request.rendaIndividual())
                .build());
        plSqlRepository.abrirCasos(pessoa.getId(), LocalDateTime.now());
        casoEventoService.publicarNovos(pessoa.getId(), Set.of());
        return DtoMapper.toResponse(pessoa);
    }

    /** Atualiza os dados e reavalia os casos ABERTO, porque renda e idade mudam o resultado da regra. */
    @Transactional
    public PessoaResponse atualizar(Long id, PessoaRequest request) {
        Pessoa pessoa = findEntityByIdAutorizado(id);
        pessoa.setNome(request.nome());
        pessoa.setDataNascimento(request.dataNascimento());
        pessoa.setRendaFamiliar(request.rendaFamiliar());
        pessoa.setQtdMembrosFamilia(request.qtdMembrosFamilia());
        pessoa.setRendaIndividual(request.rendaIndividual());
        Pessoa salva = repository.saveAndFlush(pessoa);
        Set<Long> casosAntes = casoEventoService.idsDosCasos(salva.getId());
        plSqlRepository.reavaliarCasos(salva.getId(), LocalDateTime.now());
        casoEventoService.publicarNovos(salva.getId(), casosAntes);
        return DtoMapper.toResponse(salva);
    }

    /**
     * Remove o beneficiário e tudo que é dele (receitas, orientações, casos, laudos e documentos).
     * Só é permitido enquanto todos os casos estão ABERTO: caso já em análise, protocolado ou
     * concluído tem histórico com o órgão e devolve 409. A remoção dos casos fica na auditoria do banco.
     */
    @Transactional
    public void deletar(Long id) {
        Pessoa pessoa = findEntityByIdAutorizado(id);
        if (casoRepository.existsByPessoaIdAndStatusNot(id, StatusCaso.ABERTO)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "A pessoa tem casos em andamento ou concluídos e não pode ser removida.");
        }
        receitaItemRepository.deleteByReceitaPessoaId(id);
        receitaItemRepository.flush();
        receitaRepository.deleteByPessoaId(id);
        receitaRepository.flush();
        orientacaoRepository.deleteByCasoPessoaId(id);
        orientacaoRepository.flush();
        casoRepository.deleteByPessoaId(id);
        casoRepository.flush();
        laudoRepository.deleteByPessoaId(id);
        laudoRepository.flush();
        documentoRepository.deleteByPessoaId(id);
        documentoRepository.flush();
        repository.delete(pessoa);
    }

    /** Carrega a entidade ou lança 404; usado por documento, laudo e elegibilidade. */
    @Transactional(readOnly = true)
    public Pessoa findEntityById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pessoa não encontrada: " + id));
    }

    /** Igual ao findEntityById, mas quem não é gestor só acessa os próprios beneficiários. */
    @Transactional(readOnly = true)
    public Pessoa findEntityByIdAutorizado(Long id) {
        Pessoa pessoa = findEntityById(id);
        if (!CurrentUser.isGestor() && !pessoa.getUsuario().getId().equals(CurrentUser.requireUsuarioId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você só pode acessar os próprios beneficiários.");
        }
        return pessoa;
    }
}
