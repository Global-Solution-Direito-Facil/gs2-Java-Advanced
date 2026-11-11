package br.com.fiap.direitofacil.service;

import br.com.fiap.direitofacil.entity.Caso;
import br.com.fiap.direitofacil.mensageria.EventoCaso;
import br.com.fiap.direitofacil.mensageria.EventoCasoPublisher;
import br.com.fiap.direitofacil.mensageria.TipoEventoCaso;
import br.com.fiap.direitofacil.repository.CasoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Descobre os casos que a regra PL/SQL acabou de abrir (comparando os ids antes e depois da
 * chamada) e publica um DireitoIdentificado para cada um. Também publica os eventos do monitor.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CasoEventoService {

    private final CasoRepository casoRepository;
    private final EventoCasoPublisher publisher;

    /** Ids dos casos da pessoa antes da avaliação. */
    public Set<Long> idsDosCasos(Long pessoaId) {
        return casoRepository.findByPessoaIdOrderByIdAsc(pessoaId).stream()
                .map(Caso::getId)
                .collect(Collectors.toSet());
    }

    /** Publica DireitoIdentificado para cada caso que não existia antes. */
    public void publicarNovos(Long pessoaId, Set<Long> idsAntes) {
        publicar(casoRepository.findByPessoaIdOrderByIdAsc(pessoaId).stream()
                .filter(caso -> !idsAntes.contains(caso.getId()))
                .map(caso -> EventoCaso.de(TipoEventoCaso.DIREITO_IDENTIFICADO, caso))
                .toList());
    }

    /**
     * Publica os eventos em ordem. Se, mesmo depois das novas tentativas do @Retryable, o RabbitMQ
     * continuar fora, para e só registra no log: o cadastro do cidadão não falha por causa da fila.
     */
    public void publicar(List<EventoCaso> eventos) {
        for (EventoCaso evento : eventos) {
            try {
                publisher.publicar(evento);
            } catch (AmqpException e) {
                log.warn("RabbitMQ indisponível; {} evento(s) de caso não publicado(s): {}", eventos.size(), e.getMessage());
                return;
            }
        }
    }
}
