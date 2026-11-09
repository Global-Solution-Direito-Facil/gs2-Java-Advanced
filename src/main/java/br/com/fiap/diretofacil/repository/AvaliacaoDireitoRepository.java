package br.com.fiap.diretofacil.repository;

import br.com.fiap.diretofacil.entity.AvaliacaoDireito;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AvaliacaoDireitoRepository extends JpaRepository<AvaliacaoDireito, Long> {

    /** Resultado completo de um laudo, com elegíveis primeiro, para a tela de análise do cidadão. */
    List<AvaliacaoDireito> findByDocumentoIdOrderByResultadoAsc(Long documentoId);

    /** Evita gravar a mesma avaliação duas vezes se a fila reprocessar o documento. */
    Optional<AvaliacaoDireito> findByDocumentoIdAndDireitoId(Long documentoId, Long direitoId);
}
