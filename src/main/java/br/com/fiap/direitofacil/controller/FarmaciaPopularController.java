package br.com.fiap.direitofacil.controller;

import br.com.fiap.direitofacil.dto.response.ConsultaFarmaciaPopularResponse;
import br.com.fiap.direitofacil.dto.response.MedicamentoFarmaciaPopularResponse;
import br.com.fiap.direitofacil.dto.response.PageResponse;
import br.com.fiap.direitofacil.enums.IndicacaoFarmaciaPopular;
import br.com.fiap.direitofacil.service.FarmaciaPopularService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/farmacia-popular")
@RequiredArgsConstructor
@Tag(name = "Farmácia Popular", description = "Elenco oficial do programa (41 itens) e consulta de um remédio")
public class FarmaciaPopularController {

    private final FarmaciaPopularService service;

    @GetMapping("/medicamentos")
    @Operation(summary = "Elenco do Farmácia Popular",
            description = "Lista paginada do elenco oficial (fonte e data na resposta), com filtro opcional por indicação.")
    public PageResponse<MedicamentoFarmaciaPopularResponse> elenco(
            @RequestParam(required = false) IndicacaoFarmaciaPopular indicacao,
            @ParameterObject @PageableDefault(size = 50, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        return new PageResponse<>(service.listarElenco(indicacao, pageable));
    }

    @GetMapping("/consulta")
    @Operation(summary = "Consultar um remédio",
            description = "Diz se o princípio ativo e a dosagem estão no elenco (GRATUITO, DOSAGEM_DIVERGENTE ou NAO_ENCONTRADO), sem gravar. Ignora acento e maiúsculas e trata vírgula e ponto como iguais.")
    public ConsultaFarmaciaPopularResponse consultar(
            @RequestParam @NotBlank @Size(max = 150) String principioAtivo,
            @RequestParam(required = false) @Size(max = 60) String concentracao) {
        return service.consultar(principioAtivo, concentracao);
    }
}
