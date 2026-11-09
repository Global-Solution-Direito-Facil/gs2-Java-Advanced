package br.com.fiap.diretofacil.dto.request;

import br.com.fiap.diretofacil.enums.Publico;
import br.com.fiap.diretofacil.enums.StatusConferencia;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.Set;

/** Manutenção de um direito do catálogo em POST e PUT /direitos. */
public record DireitoRequest(
        @NotBlank @Pattern(regexp = "[A-Z0-9_]{3,50}", message = "código em maiúsculas, números e _") String codigo,
        @NotBlank @Size(max = 150) String nome,
        @NotBlank @Size(max = 500) String criterios,
        @NotBlank @Size(max = 200) String ondePedir,
        @NotBlank @Size(max = 200) String baseLegal,
        @Size(max = 500) String urlBaseLegal,
        @NotEmpty Set<Publico> publicos,
        @DecimalMin("0.01") BigDecimal rendaPerCapitaMaxSm,
        @Min(0) @Max(120) Integer idadeMinima,
        @NotNull Boolean exigeCadunico,
        @NotNull Boolean exigeDeficiencia,
        @NotNull Boolean exigeTea,
        @NotNull StatusConferencia statusConferencia,
        Boolean ativo
) {}
