package br.com.fiap.direitofacil.controller;

import br.com.fiap.direitofacil.dto.request.LaudoRequest;
import br.com.fiap.direitofacil.dto.response.LaudoResponse;
import br.com.fiap.direitofacil.service.LaudoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/laudos")
@RequiredArgsConstructor
@Tag(name = "Laudos", description = "Dados estruturados dos laudos; a lista por pessoa fica em /pessoas/{id}/laudos")
public class LaudoController {

    private final LaudoService service;

    @GetMapping("/{id}")
    @Operation(summary = "Buscar laudo por ID")
    public LaudoResponse buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('GESTOR')")
    @Operation(summary = "Registrar laudo",
            description = "Exclusivo do gestor. Grava o laudo estruturado e dispara a PKG_ELEGIBILIDADE.PRC_REAVALIAR_CASOS, que atualiza os casos ABERTO e abre um caso para cada direito ELEGIVEL ou POSSIVEL da pessoa.")
    public ResponseEntity<LaudoResponse> registrar(@RequestBody @Valid LaudoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registrar(request));
    }
}
