package br.com.fiap.diretofacil.repository;

import br.com.fiap.diretofacil.entity.Direito;
import br.com.fiap.diretofacil.enums.Publico;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DireitoRepository extends JpaRepository<Direito, Long> {

    /** Localiza um direito pelo código estável (BPC_PCD, CIPTEA...), usado pelo seed e pelos testes. */
    Optional<Direito> findByCodigo(String codigo);

    /** Impede código duplicado ao criar um direito. */
    boolean existsByCodigo(String codigo);

    /** Todos os direitos ativos, que é a lista que a avaliação de elegibilidade percorre. */
    List<Direito> findByAtivoTrue();

    /** Catálogo ativo paginado para o app. */
    Page<Direito> findByAtivoTrue(Pageable pageable);

    /** Catálogo ativo filtrado por público (TEA, PCD, IDOSO, BAIXA_RENDA). */
    Page<Direito> findByAtivoTrueAndPublicosContaining(Publico publico, Pageable pageable);
}
