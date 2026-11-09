package br.com.fiap.diretofacil.specification;

import br.com.fiap.diretofacil.entity.Caso;
import br.com.fiap.diretofacil.enums.ResultadoElegibilidade;
import br.com.fiap.diretofacil.enums.StatusCaso;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/** Filtros dinâmicos do painel de casos: responsável, beneficiário, status e resultado da regra. */
public final class CasoSpecification {

    private CasoSpecification() {}

    public static Specification<Caso> filtrar(
            Long usuarioId,
            Long pessoaId,
            StatusCaso status,
            ResultadoElegibilidade resultado
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (usuarioId != null) {
                predicates.add(cb.equal(root.get("pessoa").get("usuario").get("id"), usuarioId));
            }
            if (pessoaId != null) {
                predicates.add(cb.equal(root.get("pessoa").get("id"), pessoaId));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (resultado != null) {
                predicates.add(cb.equal(root.get("resultadoAvaliacao"), resultado));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
