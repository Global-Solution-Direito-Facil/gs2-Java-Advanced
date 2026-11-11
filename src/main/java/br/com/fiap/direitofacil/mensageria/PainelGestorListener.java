package br.com.fiap.direitofacil.mensageria;

import br.com.fiap.direitofacil.service.PainelEventosService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/** Consumidor assíncrono da fila do painel: cada evento de caso vira um aviso para o assistente social. */
@Slf4j
@Component
@RequiredArgsConstructor
public class PainelGestorListener {

    private final PainelEventosService painelEventosService;

    @RabbitListener(queues = RabbitConfig.PAINEL_GESTOR_QUEUE)
    public void receber(EventoCaso evento) {
        log.info("Painel do gestor recebeu {} do caso {} ({})", evento.tipo(), evento.casoId(), evento.codigoDireito());
        painelEventosService.registrar(evento);
    }
}
