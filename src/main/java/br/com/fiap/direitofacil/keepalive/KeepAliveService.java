package br.com.fiap.direitofacil.keepalive;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Chama a própria rota /keepalive a cada minuto para o serviço não hibernar no plano gratuito
 * do Render. O endereço vem de KEEPALIVE_URL ou, no Render, de RENDER_EXTERNAL_URL (que a
 * plataforma define sozinha). Rodando local, sem nenhuma das duas, a tarefa não faz nada.
 */
@Slf4j
@Component
public class KeepAliveService {

    private final RestClient restClient = RestClient.create();
    private final String url;

    public KeepAliveService(@Value("${direitofacil.keepalive.url:}") String urlBase) {
        String base = urlBase == null ? "" : urlBase.strip().replaceAll("/+$", "");
        this.url = base.isEmpty() ? "" : base + "/keepalive";
    }

    @Scheduled(fixedRate = 60000, initialDelay = 60000)
    public void keepAlive() {
        if (url.isEmpty()) {
            return;
        }
        try {
            restClient.get()
                    .uri(url)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("Erro no Keep Alive: {}", e.getMessage());
        }
    }
}
