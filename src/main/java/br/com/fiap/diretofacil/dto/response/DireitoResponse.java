package br.com.fiap.diretofacil.dto.response;

import br.com.fiap.diretofacil.enums.Publico;
import br.com.fiap.diretofacil.enums.StatusConferencia;

import java.math.BigDecimal;
import java.util.Set;

/** Direito do catálogo com textos e critérios. */
public record DireitoResponse(
        Long id,
        String codigo,
        String nome,
        String criterios,
        String ondePedir,
        String baseLegal,
        String urlBaseLegal,
        Set<Publico> publicos,
        BigDecimal rendaPerCapitaMaxSm,
        Integer idadeMinima,
        Boolean exigeCadunico,
        Boolean exigeDeficiencia,
        Boolean exigeTea,
        StatusConferencia statusConferencia,
        Boolean ativo
) {}
