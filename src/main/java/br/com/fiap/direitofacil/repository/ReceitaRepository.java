package br.com.fiap.direitofacil.repository;

import br.com.fiap.direitofacil.entity.Receita;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acesso às receitas enviadas para a consulta do Farmácia Popular. */
public interface ReceitaRepository extends JpaRepository<Receita, Long> {

    /** Receitas de um beneficiário. */
    Page<Receita> findByPessoaId(Long pessoaId, Pageable pageable);

    /** Receitas de todos os beneficiários de um responsável, para o cidadão ver as suas. */
    Page<Receita> findByPessoaUsuarioId(Long usuarioId, Pageable pageable);
}
