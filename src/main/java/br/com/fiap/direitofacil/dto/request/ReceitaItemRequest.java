package br.com.fiap.direitofacil.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Remédio da receita depois da conferência na tela: nomeLido é o que a IA leu; principioAtivo e
 * concentracao são o que a pessoa confirmou; corrigido = a pessoa alterou a leitura da IA.
 */
public record ReceitaItemRequest(
        @NotBlank @Size(max = 150) String nomeLido,
        @NotBlank @Size(max = 150) String principioAtivo,
        @Size(max = 60) String concentracao,
        Boolean corrigido
) {}
