package br.com.fiap.direitofacil.controller;

import br.com.fiap.direitofacil.auth.UsuarioAutenticado;
import br.com.fiap.direitofacil.dto.request.LoginRequest;
import br.com.fiap.direitofacil.dto.request.TrocaSenhaRequest;
import br.com.fiap.direitofacil.dto.request.UsuarioRequest;
import br.com.fiap.direitofacil.dto.response.LoginResponse;
import br.com.fiap.direitofacil.service.AuthService;
import br.com.fiap.direitofacil.validation.EmailValidoValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Login com JWT (CIDADAO ou GESTOR), registro do cidadão e troca de senha")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final AuthService authService;

    @PostMapping("/login")
    @SecurityRequirements
    @Operation(summary = "Login",
            description = "Confere e-mail e senha e devolve o JWT (HS256, sub = e-mail, claim role = CIDADAO ou GESTOR). E-mail ou senha errados devolvem 401.")
    public LoginResponse login(@RequestBody @Valid LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(EmailValidoValidator.normalizar(request.email()), request.senha()));
        return authService.tokenPara((UsuarioAutenticado) authentication.getPrincipal());
    }

    @PostMapping("/registrar")
    @SecurityRequirements
    @Operation(summary = "Registrar cidadão",
            description = "Cria a conta com o papel CIDADAO e já devolve o token. E-mail ou CPF já cadastrados devolvem 409.")
    public ResponseEntity<LoginResponse> registrar(@RequestBody @Valid UsuarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registrar(request));
    }

    @PatchMapping("/senha")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Trocar a própria senha", description = "Exige a senha atual; senha atual errada devolve 422.")
    public void trocarSenha(@RequestBody @Valid TrocaSenhaRequest request) {
        authService.trocarSenha(request);
    }
}
