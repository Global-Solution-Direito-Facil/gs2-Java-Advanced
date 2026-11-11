package br.com.fiap.direitofacil.controller;

import br.com.fiap.direitofacil.dto.response.EventoPainelResponse;
import br.com.fiap.direitofacil.dto.response.VerificacaoCasosResponse;
import br.com.fiap.direitofacil.service.MonitorCasosService;
import br.com.fiap.direitofacil.service.PainelEventosService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/painel")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('GESTOR')")
@Tag(name = "Painel do gestor", description = "Avisos consumidos da fila do RabbitMQ: DireitoIdentificado, PrazoVencendo e CasoParado")
public class PainelController {

    private final PainelEventosService painelEventosService;
    private final MonitorCasosService monitorCasosService;

    @GetMapping("/eventos")
    @Operation(summary = "Avisos do painel",
            description = "Eventos de caso que o consumidor RabbitMQ recebeu, do mais recente para o mais antigo (até 200, em memória).")
    public List<EventoPainelResponse> eventos() {
        return painelEventosService.listar();
    }

    @PostMapping("/verificar-prazos")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "Rodar a verificação de prazos agora",
            description = "Publica PrazoVencendo (prazo nos próximos diasPrazo dias) e CasoParado (sem atualização há diasParado dias). Responde 202 Accepted: os eventos vão para a fila e o consumidor processa depois. A mesma verificação roda sozinha todo dia às 8h.")
    public VerificacaoCasosResponse verificarPrazos(
            @RequestParam(defaultValue = "7") @Min(1) @Max(365) int diasPrazo,
            @RequestParam(defaultValue = "15") @Min(1) @Max(365) int diasParado) {
        return monitorCasosService.verificar(diasPrazo, diasParado);
    }
}
