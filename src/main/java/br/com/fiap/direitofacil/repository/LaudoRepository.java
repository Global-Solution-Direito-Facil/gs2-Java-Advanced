package br.com.fiap.direitofacil.repository;

import br.com.fiap.direitofacil.entity.Laudo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/** Acesso aos laudos estruturados. */
public interface LaudoRepository extends JpaRepository<Laudo, Long> {

    /** Laudos de um beneficiário, do mais recente para o mais antigo. */
    List<Laudo> findByPessoaIdOrderByDataLaudoDesc(Long pessoaId);

    /** Laudo gerado a partir de um documento, para não extrair duas vezes. */
    Optional<Laudo> findByDocumentoId(Long documentoId);

    /** Documento que já virou laudo não pode ser removido. */
    boolean existsByDocumentoId(Long documentoId);

    /** Remove os laudos da pessoa (antes dos documentos, que eles referenciam). */
    void deleteByPessoaId(Long pessoaId);
}
