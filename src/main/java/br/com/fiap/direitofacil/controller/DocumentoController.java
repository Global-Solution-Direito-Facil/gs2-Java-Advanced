package br.com.fiap.direitofacil.controller;

import br.com.fiap.direitofacil.dto.request.DocumentoRequest;
import br.com.fiap.direitofacil.dto.response.DocumentoResponse;
import br.com.fiap.direitofacil.dto.response.PageResponse;
import br.com.fiap.direitofacil.service.DocumentoService;
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
@RequestMapping("/api/v1/documentos")
@RequiredArgsConstructor
@Tag(name = "Documentos", description = "Metadados dos laudos enviados; o arquivo fica na API .NET")
public class DocumentoController {

    private final DocumentoService service;

    @GetMapping
    @Operation(summary = "Listar documentos", description = "Cidadão vê os dos próprios beneficiários; gestor vê todos e pode filtrar por pessoa.")
    public PageResponse<DocumentoResponse> listar(
            @RequestParam(required = false) Long pessoaId,
            @ParameterObject @PageableDefault(size = 10, sort = "dataRecebimento", direction = Sort.Direction.DESC) Pageable pageable) {
        return new PageResponse<>(service.listar(pessoaId, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar documento por ID")
    public DocumentoResponse buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Excluir documento", description = "Dono ou gestor. Devolve 409 se o documento já virou laudo ou receita.")
    public void deletar(@PathVariable Long id) {
        service.deletar(id);
    }

    @PostMapping
    @Operation(summary = "Registrar documento",
            description = "Registra os metadados de um laudo recebido. Reenvio com a mesma chave de idempotência devolve o registro já existente.")
    public ResponseEntity<DocumentoResponse> registrar(@RequestBody @Valid DocumentoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registrar(request));
    }
}
