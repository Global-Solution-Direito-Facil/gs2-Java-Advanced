package br.com.fiap.diretofacil.controller;

import br.com.fiap.diretofacil.dto.request.DocumentoRequest;
import br.com.fiap.diretofacil.dto.response.DocumentoResponse;
import br.com.fiap.diretofacil.dto.response.PageResponse;
import br.com.fiap.diretofacil.service.DocumentoService;
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
@RequestMapping("/documentos")
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

    @PostMapping
    @Operation(summary = "Registrar documento",
            description = "Registra os metadados de um laudo recebido. Reenvio com a mesma chave de idempotência devolve o registro já existente.")
    public ResponseEntity<DocumentoResponse> registrar(@RequestBody @Valid DocumentoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registrar(request));
    }
}
