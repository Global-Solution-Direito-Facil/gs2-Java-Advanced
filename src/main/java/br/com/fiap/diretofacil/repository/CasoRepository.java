package br.com.fiap.diretofacil.repository;

import br.com.fiap.diretofacil.entity.Caso;
import br.com.fiap.diretofacil.enums.StatusCaso;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.List;

/** Acesso aos casos, com filtros dinâmicos via Specification. */
public interface CasoRepository extends JpaRepository<Caso, Long>, JpaSpecificationExecutor<Caso> {

    /** Casos de um beneficiário, paginados. */
    Page<Caso> findByPessoaId(Long pessoaId, Pageable pageable);

    /** Casos de todos os beneficiários de um responsável; é a lista "meus pedidos" do app. */
    Page<Caso> findByPessoaUsuarioId(Long usuarioId, Pageable pageable);

    /** Casos em andamento com prazo dentro do intervalo; alimenta o evento PrazoVencendo. */
    List<Caso> findByStatusInAndDataPrazoBetween(List<StatusCaso> status, LocalDateTime inicio, LocalDateTime fim);

    /** Casos em andamento sem atualização desde a data limite; alimenta o evento CasoParado. */
    List<Caso> findByStatusInAndDataAtualizacaoBefore(List<StatusCaso> status, LocalDateTime limite);

    /** Impede apagar um direito que já tem casos abertos. */
    boolean existsByDireitoId(Long direitoId);
}
