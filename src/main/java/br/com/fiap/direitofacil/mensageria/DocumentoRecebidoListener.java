package br.com.fiap.direitofacil.mensageria;

import br.com.fiap.direitofacil.service.ProcessamentoDocumentoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumidor da fila documentos.recebidos, publicada pela API .NET. Nunca devolve erro para a
 * fila: uma falha fica registrada no log e no status do documento, e a mensagem não volta em laço.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentoRecebidoListener {

    private final ProcessamentoDocumentoService processamentoService;

    @RabbitListener(queues = RabbitConfig.DOCUMENTOS_RECEBIDOS_QUEUE)
    public void receber(DocumentoRecebido evento) {
        log.info("DocumentoRecebido: documento {} do tipo {}", evento.documentoId(), evento.tipoDocumento());
        try {
            processamentoService.processar(evento);
        } catch (RuntimeException e) {
            log.error("Falha inesperada ao processar o documento {}", evento.documentoId(), e);
        }
    }
}
