package br.com.fiap.direitofacil.controller;

import br.com.fiap.direitofacil.dto.request.ReceitaItemRequest;
import br.com.fiap.direitofacil.dto.request.ReceitaRequest;
import br.com.fiap.direitofacil.dto.response.PageResponse;
import br.com.fiap.direitofacil.dto.response.ReceitaItemResponse;
import br.com.fiap.direitofacil.dto.response.ReceitaResponse;
import br.com.fiap.direitofacil.service.ReceitaService;
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
@RequestMapping("/api/v1/receitas")
@RequiredArgsConstructor
@Tag(name = "Receitas", description = "Minha receita: remédios conferidos pela pessoa e o que é gratuito no Farmácia Popular")
public class ReceitaController {

    private final ReceitaService service;

    @GetMapping
    @Operation(summary = "Listar receitas", description = "Cidadão vê as dos próprios beneficiários; gestor vê todas e pode filtrar por pessoa.")
    public PageResponse<ReceitaResponse> listar(
            @RequestParam(required = false) Long pessoaId,
            @ParameterObject @PageableDefault(size = 10, sort = "dataRegistro", direction = Sort.Direction.DESC) Pageable pageable) {
        return new PageResponse<>(service.listar(pessoaId, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar receita por ID", description = "Com os remédios, o resultado de cada um e quantos são gratuitos.")
    public ReceitaResponse buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    @Operation(summary = "Abrir receita", description = "Cria a receita da pessoa pela PKG_FARMACIA_POPULAR; documentoId é a foto já registrada (opcional).")
    public ResponseEntity<ReceitaResponse> criar(@RequestBody @Valid ReceitaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(request));
    }

    @PostMapping("/{id}/itens")
    @Operation(summary = "Adicionar remédio conferido",
            description = "Chamar só depois que a pessoa conferiu a leitura da IA na tela. A tabela do elenco decide GRATUITO, DOSAGEM_DIVERGENTE ou NAO_ENCONTRADO; o app nunca sugere trocar o remédio.")
    public ResponseEntity<ReceitaItemResponse> adicionarItem(@PathVariable Long id, @RequestBody @Valid ReceitaItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.adicionarItem(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Excluir receita", description = "Remove a receita e os remédios; dono ou gestor.")
    public void deletar(@PathVariable Long id) {
        service.deletar(id);
    }
}
