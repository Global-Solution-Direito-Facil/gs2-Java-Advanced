package br.com.fiap.direitofacil.mensageria;

/** Eventos de caso publicados pelo Java no RabbitMQ; cada um tem a própria routing key. */
public enum TipoEventoCaso {
    DIREITO_IDENTIFICADO(RabbitConfig.DIREITO_IDENTIFICADO_ROUTING_KEY),
    PRAZO_VENCENDO(RabbitConfig.PRAZO_VENCENDO_ROUTING_KEY),
    CASO_PARADO(RabbitConfig.CASO_PARADO_ROUTING_KEY);

    private final String routingKey;

    TipoEventoCaso(String routingKey) {
        this.routingKey = routingKey;
    }

    public String routingKey() {
        return routingKey;
    }
}
