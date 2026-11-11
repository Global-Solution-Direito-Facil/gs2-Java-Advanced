package br.com.fiap.direitofacil.repository;

import br.com.fiap.direitofacil.entity.ReceitaItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Acesso aos remédios conferidos de cada receita. */
public interface ReceitaItemRepository extends JpaRepository<ReceitaItem, Long> {

    /** Itens de uma receita na ordem em que foram conferidos. */
    List<ReceitaItem> findByReceitaIdOrderByIdAsc(Long receitaId);
}
