package br.com.fiap.direitofacil.repository;

import br.com.fiap.direitofacil.entity.Pessoa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acesso aos beneficiários. */
public interface PessoaRepository extends JpaRepository<Pessoa, Long> {

    /** Beneficiários de um responsável; é a lista "meus familiares" do app. */
    Page<Pessoa> findByUsuarioId(Long usuarioId, Pageable pageable);

    /** Busca por parte do nome na tela do gestor, paginada. */
    Page<Pessoa> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
}
