package br.com.fiap.diretofacil.repository;

import br.com.fiap.diretofacil.entity.Caso;
import br.com.fiap.diretofacil.enums.StatusCaso;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface CasoRepository extends JpaRepository<Caso, Long>, JpaSpecificationExecutor<Caso> {

    /** Casos de um cidadão, paginados; é a lista "meus pedidos" do app. */
    Page<Caso> findByCidadaoId(Long cidadaoId, Pageable pageable);

    /** Casos em andamento com prazo dentro do intervalo; alimenta o evento PrazoVencendo. */
    List<Caso> findByStatusInAndPrazoBetween(List<StatusCaso> status, LocalDate inicio, LocalDate fim);

    /** Casos em andamento sem atualização desde a data limite; alimenta o evento CasoParado. */
    List<Caso> findByStatusInAndDataAtualizacaoBefore(List<StatusCaso> status, LocalDateTime limite);

    /** Evita abrir dois casos para a mesma avaliação positiva. */
    boolean existsByAvaliacaoId(Long avaliacaoId);
}
