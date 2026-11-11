package br.com.fiap.direitofacil.service;

import br.com.fiap.direitofacil.dto.response.VerificacaoCasosResponse;
import br.com.fiap.direitofacil.entity.Caso;
import br.com.fiap.direitofacil.enums.StatusCaso;
import br.com.fiap.direitofacil.mensageria.EventoCaso;
import br.com.fiap.direitofacil.mensageria.TipoEventoCaso;
import br.com.fiap.direitofacil.repository.CasoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Verificação periódica dos casos em andamento: publica PrazoVencendo para os que vencem nos
 * próximos dias e CasoParado para os que estão sem atualização há muito tempo.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MonitorCasosService {

    /** Casos que ainda dependem de ação (CONCEDIDO e NEGADO são finais). */
    private static final List<StatusCaso> EM_ANDAMENTO = List.of(
            StatusCaso.ABERTO, StatusCaso.EM_ANALISE, StatusCaso.DOCUMENTOS_PENDENTES, StatusCaso.PROTOCOLADO);

    private final CasoRepository casoRepository;
    private final CasoEventoService casoEventoService;

    @Value("${direitofacil.monitor.dias-prazo}")
    private int diasPrazoPadrao;

    @Value("${direitofacil.monitor.dias-parado}")
    private int diasParadoPadrao;

    /** Roda no horário configurado (padrão: todo dia às 8h). */
    @Scheduled(cron = "${direitofacil.monitor.cron}", zone = "America/Sao_Paulo")
    public void verificarAgendado() {
        VerificacaoCasosResponse resultado = verificar(diasPrazoPadrao, diasParadoPadrao);
        log.info("Monitor de casos: {} prazo(s) vencendo e {} caso(s) parado(s)",
                resultado.prazoVencendo(), resultado.casosParados());
    }

    /** Também usada pelo gestor para rodar na hora, com janelas diferentes (demonstração). */
    @Transactional(readOnly = true)
    public VerificacaoCasosResponse verificar(int diasPrazo, int diasParado) {
        LocalDateTime agora = LocalDateTime.now();
        List<Caso> vencendo = casoRepository.findByStatusInAndDataPrazoBetween(EM_ANDAMENTO, agora, agora.plusDays(diasPrazo));
        List<Caso> parados = casoRepository.findByStatusInAndDataAtualizacaoBefore(EM_ANDAMENTO, agora.minusDays(diasParado));

        List<EventoCaso> eventos = new ArrayList<>();
        vencendo.forEach(caso -> eventos.add(EventoCaso.de(TipoEventoCaso.PRAZO_VENCENDO, caso)));
        parados.forEach(caso -> eventos.add(EventoCaso.de(TipoEventoCaso.CASO_PARADO, caso)));
        casoEventoService.publicar(eventos);
        return new VerificacaoCasosResponse(vencendo.size(), parados.size(), diasPrazo, diasParado);
    }
}
