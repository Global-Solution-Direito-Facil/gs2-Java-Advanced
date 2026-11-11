package br.com.fiap.direitofacil.controller;

import br.com.fiap.direitofacil.dto.request.PessoaRequest;
import br.com.fiap.direitofacil.dto.response.AvaliacaoDireitoResponse;
import br.com.fiap.direitofacil.dto.response.LaudoResponse;
import br.com.fiap.direitofacil.dto.response.PageResponse;
import br.com.fiap.direitofacil.dto.response.PessoaResponse;
import br.com.fiap.direitofacil.service.ElegibilidadeService;
import br.com.fiap.direitofacil.service.LaudoService;
import br.com.fiap.direitofacil.service.PessoaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pessoas")
@RequiredArgsConstructor
@Tag(name = "Pessoas", description = "Beneficiários acompanhados por um usuário responsável")
public class PessoaController {

    private final PessoaService service;
    private final LaudoService laudoService;
    private final ElegibilidadeService elegibilidadeService;

    @GetMapping
    @Operation(summary = "Listar pessoas", description = "Cidadão vê os próprios beneficiários; gestor vê todos e pode filtrar por nome.")
    public PageResponse<PessoaResponse> listar(
            @RequestParam(required = false) String nome,
            @ParameterObject @PageableDefault(size = 10, sort = "nome", direction = Sort.Direction.ASC) Pageable pageable) {
        return new PageResponse<>(service.listar(nome, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar pessoa por ID")
    public PessoaResponse buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    @Operation(summary = "Cadastrar pessoa", description = "Cidadão cadastra sob a própria conta; gestor informa o responsável em usuarioId. Já abre os casos ELEGIVEL e POSSIVEL pela regra PL/SQL. rendaIndividual nula = 'não sei'.")
    public ResponseEntity<PessoaResponse> criar(
            @RequestBody @Valid PessoaRequest request,
            @RequestParam(required = false) Long usuarioId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(request, usuarioId));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar pessoa", description = "Altera nome, nascimento, rendas e número de membros, e reavalia os casos ABERTO pela PKG_ELEGIBILIDADE.PRC_REAVALIAR_CASOS.")
    public PessoaResponse atualizar(@PathVariable Long id, @RequestBody @Valid PessoaRequest request) {
        return service.atualizar(id, request);
    }

    @GetMapping("/{id}/laudos")
    @Operation(summary = "Laudos da pessoa", description = "Do mais recente para o mais antigo.")
    public List<LaudoResponse> laudos(@PathVariable Long id) {
        return laudoService.listarPorPessoa(id);
    }

    @GetMapping("/{id}/elegibilidade")
    @Operation(summary = "Avaliar elegibilidade",
            description = "Avalia todo o catálogo de direitos na hora pela PKG_ELEGIBILIDADE do Oracle e devolve o resultado e o motivo de cada um, inclusive os NAO_ELEGIVEL. Nada é gravado.")
    public List<AvaliacaoDireitoResponse> elegibilidade(@PathVariable Long id) {
        return elegibilidadeService.avaliarTodos(id);
    }
}
