package br.com.fiap.direitofacil.controller;

import br.com.fiap.direitofacil.dto.request.ExtracaoReceitaRequest;
import br.com.fiap.direitofacil.dto.response.MedicamentoLidoResponse;
import br.com.fiap.direitofacil.ia.IaReceitaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/receitas")
@RequiredArgsConstructor
@Tag(name = "IA - Receita", description = "Leitura da foto da receita com Spring AI (Gemini)")
public class IaReceitaController {

    private final IaReceitaService service;

    @PostMapping("/extracao")
    @Operation(summary = "Ler a receita com IA",
            description = "Baixa a foto da receita na API .NET (documentoId) e devolve os remédios lidos, para a pessoa conferir na tela. Não grava nada e não diz se o remédio é gratuito: isso é decidido depois pela tabela do Farmácia Popular. 422 se o arquivo não for uma receita legível; 503 se a IA estiver indisponível.")
    public List<MedicamentoLidoResponse> extrair(@RequestBody @Valid ExtracaoReceitaRequest request) {
        return service.extrair(request.documentoId());
    }
}
