package br.com.fiap.direitofacil.controller;

import br.com.fiap.direitofacil.dto.request.DireitoRequest;
import br.com.fiap.direitofacil.dto.response.DireitoResponse;
import br.com.fiap.direitofacil.dto.response.PageResponse;
import br.com.fiap.direitofacil.enums.GrupoAlvo;
import br.com.fiap.direitofacil.service.DireitoService;
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

@RestController
@RequestMapping("/api/v1/direitos")
@RequiredArgsConstructor
@Tag(name = "Direitos", description = "Catálogo de direitos federais e seus critérios")
public class DireitoController {

    private final DireitoService service;

    @GetMapping
    @Operation(summary = "Listar direitos", description = "Catálogo paginado, com filtro opcional por grupo alvo.")
    public PageResponse<DireitoResponse> listar(
            @RequestParam(required = false) GrupoAlvo grupoAlvo,
            @ParameterObject @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        return new PageResponse<>(service.listar(grupoAlvo, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar direito por ID")
    public DireitoResponse buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @GetMapping("/codigo/{codigo}")
    @Operation(summary = "Buscar direito por código", description = "Ex.: BPC_PCD, CIPTEA, TARIFA_SOCIAL_ENERGIA.")
    public DireitoResponse buscarPorCodigo(@PathVariable String codigo) {
        return service.buscarPorCodigo(codigo);
    }

    @PostMapping
    @Operation(summary = "Criar direito", description = "Exclusivo do gestor; cria o direito e o critério de elegibilidade.")
    public ResponseEntity<DireitoResponse> criar(@RequestBody @Valid DireitoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar direito", description = "Exclusivo do gestor; atualiza o direito e o critério.")
    public DireitoResponse atualizar(@PathVariable Long id, @RequestBody @Valid DireitoRequest request) {
        return service.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Excluir direito", description = "Exclusivo do gestor; devolve 409 se já houver casos ligados ao direito.")
    public void deletar(@PathVariable Long id) {
        service.deletar(id);
    }
}
