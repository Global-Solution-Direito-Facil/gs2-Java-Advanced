package br.com.fiap.direitofacil.keepalive;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Tag(name = "Keep Alive", description = "Rota pública que mantém a API acordada no Render e serve de verificação de saúde para a API .NET")
public class KeepAliveController {

    @GetMapping("/keepalive")
    @SecurityRequirements
    @Operation(summary = "Keep Alive", description = "Responde UP sem exigir token e sem consultar o banco.")
    public ResponseEntity<Map<String, String>> keepAlive() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "message", "Olá! Você está na página de Keep Alive do Direito Fácil."
        ));
    }
}
