package br.com.fiap.direitofacil.repository;

import br.com.fiap.direitofacil.entity.ReceitaItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/** Acesso aos remédios conferidos de cada receita. */
public interface ReceitaItemRepository extends JpaRepository<ReceitaItem, Long> {

    /** Itens de uma receita na ordem em que foram conferidos. */
    List<ReceitaItem> findByReceitaIdOrderByIdAsc(Long receitaId);

    /** Último item gravado na receita (o PRC_ADICIONAR_ITEM não devolve o id). */
    Optional<ReceitaItem> findTopByReceitaIdOrderByIdDesc(Long receitaId);

    /** Remove os itens de uma receita. */
    void deleteByReceitaId(Long receitaId);

    /** Remove os itens de todas as receitas de uma pessoa. */
    void deleteByReceitaPessoaId(Long pessoaId);
}
