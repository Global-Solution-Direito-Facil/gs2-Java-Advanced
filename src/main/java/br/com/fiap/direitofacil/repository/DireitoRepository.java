package br.com.fiap.direitofacil.repository;

import br.com.fiap.direitofacil.entity.Direito;
import br.com.fiap.direitofacil.enums.GrupoAlvo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** Acesso ao catálogo de direitos. */
public interface DireitoRepository extends JpaRepository<Direito, Long> {

    /** Localiza um direito pelo código estável (BPC_PCD, CIPTEA...). */
    Optional<Direito> findByCodigo(String codigo);

    /** Impede código duplicado ao criar um direito. */
    boolean existsByCodigo(String codigo);

    /** Catálogo filtrado por público (AUTISMO, PCD, IDOSO, BAIXA_RENDA). */
    Page<Direito> findByGrupoAlvo(GrupoAlvo grupoAlvo, Pageable pageable);
}
