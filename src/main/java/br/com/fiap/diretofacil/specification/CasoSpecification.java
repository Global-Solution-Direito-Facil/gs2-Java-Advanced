package br.com.fiap.diretofacil.specification;

import br.com.fiap.diretofacil.entity.Caso;
import br.com.fiap.diretofacil.enums.ResultadoElegibilidade;
import br.com.fiap.diretofacil.enums.StatusCaso;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/** Filtros dinâmicos do painel de casos: cidadão, status, resultado da avaliação e gestor responsável. */
public final class CasoSpecification {

    private CasoSpecification() {}

    public static Specification<Caso> filtrar(
            Long cidadaoId,
            StatusCaso status,
            ResultadoElegibilidade resultado,
            Long gestorId
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (cidadaoId != null) {
                predicates.add(cb.equal(root.get("cidadao").get("id"), cidadaoId));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (resultado != null) {
                predicates.add(cb.equal(root.get("avaliacao").get("resultado"), resultado));
            }
            if (gestorId != null) {
                predicates.add(cb.equal(root.get("gestorResponsavel").get("id"), gestorId));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
