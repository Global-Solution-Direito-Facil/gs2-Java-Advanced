package br.com.fiap.diretofacil.repository;

import br.com.fiap.diretofacil.entity.Documento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DocumentoRepository extends JpaRepository<Documento, Long> {

    /** Detecta reenvio do mesmo upload pelo header Idempotency-Key e devolve o documento já salvo. */
    Optional<Documento> findByIdempotencyKey(String idempotencyKey);

    Page<Documento> findByCidadaoId(Long cidadaoId, Pageable pageable);
}
