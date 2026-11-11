package br.com.fiap.direitofacil.service;

import br.com.fiap.direitofacil.dto.response.EventoPainelResponse;
import br.com.fiap.direitofacil.mensageria.EventoCaso;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Avisos recebidos pela fila do painel do gestor, guardados em memória (os mais recentes
 * primeiro, até 200). Não precisa de tabela: o histórico oficial do caso é a auditoria do banco.
 */
@Service
public class PainelEventosService {

    private static final int LIMITE = 200;

    private final ConcurrentLinkedDeque<EventoPainelResponse> eventos = new ConcurrentLinkedDeque<>();

    public void registrar(EventoCaso evento) {
        eventos.addFirst(new EventoPainelResponse(evento, LocalDateTime.now()));
        while (eventos.size() > LIMITE) {
            eventos.pollLast();
        }
    }

    public List<EventoPainelResponse> listar() {
        return List.copyOf(eventos);
    }
}
