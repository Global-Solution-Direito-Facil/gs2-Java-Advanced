package br.com.fiap.diretofacil.controller;

import br.com.fiap.diretofacil.dto.request.UsuarioUpdateRequest;
import br.com.fiap.diretofacil.dto.response.UsuarioResponse;
import br.com.fiap.diretofacil.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuários", description = "Conta de quem está logado")
public class UsuarioController {

    private final UsuarioService service;

    @GetMapping("/me")
    @Operation(summary = "Minha conta", description = "Devolve a conta do usuário logado, lendo o id do token.")
    public UsuarioResponse meuPerfil() {
        return service.meuPerfil();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar usuário por ID", description = "Cidadão só acessa a própria conta; gestor acessa qualquer uma.")
    public UsuarioResponse buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar usuário", description = "Altera o nome; CPF, email e senha não mudam por aqui.")
    public UsuarioResponse atualizar(@PathVariable Long id, @RequestBody @Valid UsuarioUpdateRequest request) {
        return service.atualizar(id, request);
    }
}
