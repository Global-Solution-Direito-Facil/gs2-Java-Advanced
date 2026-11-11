package br.com.fiap.direitofacil.dto.request;

import br.com.fiap.direitofacil.enums.TipoCondicao;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

/** Registro de um laudo já estruturado, pelo gestor ou pela extração da IA. */
public record LaudoRequest(
        @NotNull Long pessoaId,
        Long documentoId,
        @NotBlank @Size(max = 10) String codCid,
        @NotBlank @Size(max = 200) String condicao,
        @NotNull TipoCondicao tipoCondicao,
        @NotNull @PastOrPresent LocalDate dataLaudo
) {}
