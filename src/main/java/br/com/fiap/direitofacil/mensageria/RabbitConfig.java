package br.com.fiap.direitofacil.mensageria;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Topologia do RabbitMQ (requisito 5, opção A), no padrão da aula: exchange do tipo topic,
 * fila durável (as mensagens sobrevivem a um reinício do RabbitMQ) e binding pela routing key.
 * A fila do painel recebe todos os eventos de caso ("caso.#").
 */
@Configuration
public class RabbitConfig {

    public static final String CASOS_EXCHANGE = "direitofacil.casos.exchange";
    public static final String PAINEL_GESTOR_QUEUE = "direitofacil.painel-gestor";

    /** Fila da API .NET (exchange padrão): um evento DocumentoRecebido a cada upload. */
    public static final String DOCUMENTOS_RECEBIDOS_QUEUE = "documentos.recebidos";

    public static final String DIREITO_IDENTIFICADO_ROUTING_KEY = "caso.direito-identificado";
    public static final String PRAZO_VENCENDO_ROUTING_KEY = "caso.prazo-vencendo";
    public static final String CASO_PARADO_ROUTING_KEY = "caso.parado";

    @Bean
    Queue painelGestorQueue() {
        return QueueBuilder.durable(PAINEL_GESTOR_QUEUE).build();
    }

    /** Mesma declaração da .NET (durável); assim o consumidor sobe mesmo antes do primeiro upload. */
    @Bean
    Queue documentosRecebidosQueue() {
        return QueueBuilder.durable(DOCUMENTOS_RECEBIDOS_QUEUE).build();
    }

    @Bean
    TopicExchange casosExchange() {
        return new TopicExchange(CASOS_EXCHANGE);
    }

    @Bean
    Binding painelGestorBinding() {
        return BindingBuilder
                .bind(painelGestorQueue())
                .to(casosExchange())
                .with("caso.#");
    }

    /** O evento vai como JSON (a aula envia só uma String; aqui o painel precisa do caso inteiro). */
    @Bean
    MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
