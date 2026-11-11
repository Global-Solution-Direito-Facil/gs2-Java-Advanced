package br.com.fiap.direitofacil.repository;

import br.com.fiap.direitofacil.entity.Pessoa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Acesso aos beneficiários. */
public interface PessoaRepository extends JpaRepository<Pessoa, Long> {

    /** Beneficiários de um responsável; é a lista "meus familiares" do app. */
    Page<Pessoa> findByUsuarioId(Long usuarioId, Pageable pageable);

    /** Busca por parte do nome na tela do gestor, paginada. */
    Page<Pessoa> findByNomeContainingIgnoreCase(String nome, Pageable pageable);

    /** Beneficiários da conta com este e-mail; usado para achar de quem é o documento que veio pela fila. */
    List<Pessoa> findTop2ByUsuarioEmailIgnoreCaseOrderByIdAsc(String email);

    /** Confere se o beneficiário pertence à conta com este e-mail. */
    boolean existsByIdAndUsuarioEmailIgnoreCase(Long id, String email);
}
