package br.com.fiap.direitofacil.mensageria;

import br.com.fiap.direitofacil.ia.IaLeituraException;
import br.com.fiap.direitofacil.ia.IaOrientacaoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/**
 * Consumidor do evento DireitoIdentificado: cada caso recém-aberto recebe o passo a passo
 * gerado com RAG, sem ninguém precisar pedir. Falha fica no log e a mensagem não volta para a
 * fila; a orientação ainda pode ser gerada depois por POST /casos/{id}/orientacao/gerar.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrientacaoIaListener {

    private static final int TENTATIVAS = 3;
    private static final long ESPERA_MS = 1500;

    private final IaOrientacaoService iaOrientacaoService;

    @RabbitListener(queues = RabbitConfig.ORIENTACAO_IA_QUEUE)
    public void receber(EventoCaso evento) {
        try {
            boolean gerada = gerarQuandoOCasoExistir(evento.casoId());
            log.info("Orientação do caso {} ({}): {}", evento.casoId(), evento.codigoDireito(), gerada ? "gerada" : "já existia");
        } catch (IaLeituraException | ResponseStatusException e) {
            log.warn("Orientação do caso {} não foi gerada: {}", evento.casoId(), e.getMessage());
        } catch (RuntimeException e) {
            log.error("Falha inesperada ao gerar a orientação do caso {}", evento.casoId(), e);
        }
    }

    /** O evento é publicado antes do commit de quem abriu o caso; por isso espera um pouco se ele ainda não aparece. */
    private boolean gerarQuandoOCasoExistir(Long casoId) {
        for (int tentativa = 1; ; tentativa++) {
            try {
                return iaOrientacaoService.gerarParaCasoNovo(casoId);
            } catch (ResponseStatusException e) {
                if (e.getStatusCode().value() != 404 || tentativa == TENTATIVAS) {
                    throw e;
                }
                esperar();
            }
        }
    }

    private void esperar() {
        try {
            Thread.sleep(ESPERA_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
