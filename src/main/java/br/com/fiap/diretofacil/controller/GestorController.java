package br.com.fiap.diretofacil.controller;

import br.com.fiap.diretofacil.dto.request.UsuarioRequest;
import br.com.fiap.diretofacil.dto.response.PageResponse;
import br.com.fiap.diretofacil.dto.response.UsuarioResponse;
import br.com.fiap.diretofacil.service.UsuarioService;
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
@RequestMapping("/gestores")
@RequiredArgsConstructor
@Tag(name = "Gestores", description = "Equipe de assistentes sociais; exclusivo do papel GESTOR")
public class GestorController {

    private final UsuarioService service;

    @GetMapping
    @Operation(summary = "Listar gestores", description = "Lista a equipe com paginação e ordenação.")
    public PageResponse<UsuarioResponse> listar(
            @ParameterObject @PageableDefault(size = 10, sort = "nome", direction = Sort.Direction.ASC) Pageable pageable) {
        return new PageResponse<>(service.listarGestores(pageable));
    }

    @PostMapping
    @Operation(summary = "Criar gestor", description = "Um gestor logado cria a conta de outro gestor.")
    public ResponseEntity<UsuarioResponse> criar(@RequestBody @Valid UsuarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criarGestor(request));
    }
}
