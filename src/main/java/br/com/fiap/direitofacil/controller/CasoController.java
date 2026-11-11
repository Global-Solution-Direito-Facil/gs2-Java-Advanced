package br.com.fiap.direitofacil.controller;

import br.com.fiap.direitofacil.dto.request.AtualizarStatusCasoRequest;
import br.com.fiap.direitofacil.dto.request.RegistrarOrientacaoRequest;
import br.com.fiap.direitofacil.dto.response.CasoResponse;
import br.com.fiap.direitofacil.dto.response.OrientacaoResponse;
import br.com.fiap.direitofacil.dto.response.PageResponse;
import br.com.fiap.direitofacil.enums.ResultadoElegibilidade;
import br.com.fiap.direitofacil.enums.StatusCaso;
import br.com.fiap.direitofacil.service.CasoService;
import br.com.fiap.direitofacil.service.OrientacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/casos")
@RequiredArgsConstructor
@Tag(name = "Casos", description = "Pedidos de direitos acompanhados até a concessão")
public class CasoController {

    private final CasoService service;
    private final OrientacaoService orientacaoService;

    @GetMapping
    @Operation(summary = "Listar casos",
            description = "Filtros combináveis por pessoa, status e resultado da avaliação. Cidadão só enxerga os casos dos próprios beneficiários.")
    public PageResponse<CasoResponse> listar(
            @RequestParam(required = false) Long pessoaId,
            @RequestParam(required = false) StatusCaso status,
            @RequestParam(required = false) ResultadoElegibilidade resultado,
            @ParameterObject @PageableDefault(size = 10, sort = "dataPrazo", direction = Sort.Direction.ASC) Pageable pageable) {
        return new PageResponse<>(service.listar(pessoaId, status, resultado, pageable));
    }

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('CIDADAO')")
    @Operation(summary = "Meus casos", description = "Casos de todos os beneficiários do cidadão logado.")
    public PageResponse<CasoResponse> meusCasos(
            @ParameterObject @PageableDefault(size = 10, sort = "dataPrazo", direction = Sort.Direction.ASC) Pageable pageable) {
        return new PageResponse<>(service.meusCasos(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar caso por ID")
    public CasoResponse buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('GESTOR')")
    @Operation(summary = "Atualizar status do caso",
            description = "Exclusivo do gestor. Passa pela PKG_CASOS: o gatilho do banco valida o fluxo (PROTOCOLADO só vai para CONCEDIDO ou NEGADO; estados finais não mudam) e grava a auditoria.")
    public CasoResponse atualizarStatus(@PathVariable Long id, @RequestBody @Valid AtualizarStatusCasoRequest request) {
        return service.atualizarStatus(id, request);
    }

    @GetMapping("/{id}/orientacao")
    @Operation(summary = "Orientação do caso", description = "Passo a passo em linguagem simples, com a fonte citada.")
    public OrientacaoResponse orientacao(@PathVariable Long id) {
        return orientacaoService.buscarPorCaso(id);
    }

    @PutMapping("/{id}/orientacao")
    @PreAuthorize("hasAuthority('GESTOR')")
    @Operation(summary = "Registrar orientação", description = "Exclusivo do gestor. Grava ou substitui o passo a passo do caso.")
    public OrientacaoResponse registrarOrientacao(@PathVariable Long id, @RequestBody @Valid RegistrarOrientacaoRequest request) {
        return orientacaoService.registrar(id, request);
    }
}
