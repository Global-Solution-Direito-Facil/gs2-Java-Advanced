package br.com.fiap.direitofacil.repository;

import br.com.fiap.direitofacil.entity.CriterioDireito;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** Acesso aos critérios de elegibilidade. */
public interface CriterioDireitoRepository extends JpaRepository<CriterioDireito, Long> {

    /** Critério de um direito, para o DireitoService atualizar junto com o direito. */
    Optional<CriterioDireito> findByDireitoId(Long direitoId);
}
