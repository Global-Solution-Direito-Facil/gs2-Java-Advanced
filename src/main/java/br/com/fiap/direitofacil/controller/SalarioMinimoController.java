package br.com.fiap.direitofacil.controller;

import br.com.fiap.direitofacil.dto.response.SalarioMinimoResponse;
import br.com.fiap.direitofacil.service.SalarioMinimoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/salario-minimo")
@RequiredArgsConstructor
@Tag(name = "Salário mínimo", description = "Referência dos critérios de renda")
public class SalarioMinimoController {

    private final SalarioMinimoService service;

    @GetMapping
    @Operation(summary = "Salário mínimo vigente", description = "O valor mais recente, que a regra de elegibilidade usa para calcular a renda per capita.")
    public SalarioMinimoResponse atual() {
        return service.atual();
    }
}
