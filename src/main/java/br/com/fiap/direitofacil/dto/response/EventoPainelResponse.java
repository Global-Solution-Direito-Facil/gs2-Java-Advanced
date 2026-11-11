package br.com.fiap.direitofacil.dto.response;

import br.com.fiap.direitofacil.mensageria.EventoCaso;

import java.time.LocalDateTime;

/** Aviso do painel do gestor: o evento consumido da fila e quando chegou. */
public record EventoPainelResponse(
        EventoCaso evento,
        LocalDateTime recebidoEm
) {}
