package br.com.fiap.diretofacil.dto.request;

import br.com.fiap.diretofacil.enums.GrupoAlvo;
import br.com.fiap.diretofacil.enums.StatusVerificacao;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

/** Manutenção de um direito do catálogo e do seu critério em POST e PUT /direitos. */
public record DireitoRequest(
        @NotBlank @Pattern(regexp = "[A-Za-z0-9_]{3,40}", message = "use letras, números e _ (3 a 40)") String codigo,
        @NotBlank @Size(max = 150) String nome,
        @NotBlank @Size(max = 100) String orgao,
        @NotBlank @Size(max = 200) String baseLegal,
        @NotNull GrupoAlvo grupoAlvo,
        @NotNull @Positive Integer prazoDias,
        @NotNull StatusVerificacao statusVerificacao,
        @NotNull @Valid CriterioDireitoRequest criterio
) {}
