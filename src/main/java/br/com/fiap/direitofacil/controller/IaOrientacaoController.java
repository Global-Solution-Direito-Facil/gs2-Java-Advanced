package br.com.fiap.direitofacil.controller;

import br.com.fiap.direitofacil.dto.response.OrientacaoResponse;
import br.com.fiap.direitofacil.ia.IaOrientacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/casos")
@RequiredArgsConstructor
@Tag(name = "IA - Orientação", description = "Passo a passo do caso gerado com RAG (Spring AI + base de conhecimento)")
public class IaOrientacaoController {

    private final IaOrientacaoService service;

    @PostMapping("/{id}/orientacao/gerar")
    @Operation(summary = "Gerar o passo a passo com IA",
            description = "Busca na base de conhecimento os trechos sobre o direito do caso, gera o passo a passo em linguagem simples e grava com a fonte citada, substituindo a orientação anterior. Dono do caso ou gestor. 422 se a base não tem orientação para o direito; 503 se a IA estiver indisponível. Casos novos recebem a orientação sozinhos, pelo evento DireitoIdentificado.")
    public OrientacaoResponse gerar(@PathVariable Long id) {
        return service.gerar(id);
    }
}
