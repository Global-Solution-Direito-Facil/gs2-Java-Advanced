package br.com.fiap.direitofacil.repository;

import br.com.fiap.direitofacil.entity.SalarioMinimo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** Acesso aos valores de salário mínimo. */
public interface SalarioMinimoRepository extends JpaRepository<SalarioMinimo, Integer> {

    /** Salário mínimo mais recente, o mesmo que a função PL/SQL usa. */
    Optional<SalarioMinimo> findTopByOrderByAnoReferenciaDesc();
}
