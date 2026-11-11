package br.com.fiap.direitofacil.repository;

import br.com.fiap.direitofacil.entity.Orientacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** Acesso às orientações geradas pela IA. */
public interface OrientacaoRepository extends JpaRepository<Orientacao, Long> {

    /** Orientação de um caso, para a tela de detalhe. */
    Optional<Orientacao> findByCasoId(Long casoId);

    /** Remove as orientações dos casos de uma pessoa, antes de remover os casos. */
    void deleteByCasoPessoaId(Long pessoaId);
}
