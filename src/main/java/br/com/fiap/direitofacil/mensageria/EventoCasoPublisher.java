package br.com.fiap.direitofacil.mensageria;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Component;

/**
 * Publica um evento de caso no exchange com rabbitTemplate.convertAndSend, como na aula.
 * Se o RabbitMQ falhar, tenta de novo com backoff exponencial (@Retryable da aula de Resiliência).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EventoCasoPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Retryable(
            includes = AmqpException.class,
            maxRetries = 3,
            delay = 500,
            jitter = 50,
            multiplier = 2,
            maxDelay = 4000
    )
    public void publicar(EventoCaso evento) {
        rabbitTemplate.convertAndSend(RabbitConfig.CASOS_EXCHANGE, evento.tipo().routingKey(), evento);
        log.info("Evento {} publicado para o caso {}", evento.tipo(), evento.casoId());
    }
}
