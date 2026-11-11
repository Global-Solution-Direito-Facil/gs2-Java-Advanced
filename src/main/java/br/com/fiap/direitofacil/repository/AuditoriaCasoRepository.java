package br.com.fiap.direitofacil.repository;

import br.com.fiap.direitofacil.entity.AuditoriaCaso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Leitura da trilha de auditoria gravada pelo gatilho do banco. */
public interface AuditoriaCasoRepository extends JpaRepository<AuditoriaCaso, Long> {

    /** Histórico de um caso, do mais antigo para o mais recente. */
    List<AuditoriaCaso> findByCasoIdOrderByIdAsc(Long casoId);
}
