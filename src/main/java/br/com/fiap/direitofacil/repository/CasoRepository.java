package br.com.fiap.direitofacil.repository;

import br.com.fiap.direitofacil.entity.Caso;
import br.com.fiap.direitofacil.enums.StatusCaso;
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

    /** Todos os casos de um beneficiário, para descobrir quais a regra PL/SQL acabou de abrir. */
    List<Caso> findByPessoaIdOrderByIdAsc(Long pessoaId);

    /** Casos de todos os beneficiários de um responsável; é a lista "meus pedidos" do app. */
    Page<Caso> findByPessoaUsuarioId(Long usuarioId, Pageable pageable);

    /** Casos em andamento com prazo dentro do intervalo; alimenta o evento PrazoVencendo. */
    List<Caso> findByStatusInAndDataPrazoBetween(List<StatusCaso> status, LocalDateTime inicio, LocalDateTime fim);

    /** Casos em andamento sem atualização desde a data limite; alimenta o evento CasoParado. */
    List<Caso> findByStatusInAndDataAtualizacaoBefore(List<StatusCaso> status, LocalDateTime limite);

    /** Verdadeiro se a pessoa tem algum caso que já saiu de ABERTO (em análise, protocolado, concedido...). */
    boolean existsByPessoaIdAndStatusNot(Long pessoaId, StatusCaso status);

    /** Remove os casos da pessoa (o gatilho do banco registra o DELETE na auditoria). */
    void deleteByPessoaId(Long pessoaId);

    /** Impede apagar um direito que já tem casos abertos. */
    boolean existsByDireitoId(Long direitoId);
}
