package br.com.fiap.direitofacil.repository;

import br.com.fiap.direitofacil.entity.MedicamentoFarmaciaPopular;
import br.com.fiap.direitofacil.enums.IndicacaoFarmaciaPopular;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acesso ao elenco do Farmácia Popular (só leitura pela API). */
public interface MedicamentoFarmaciaPopularRepository extends JpaRepository<MedicamentoFarmaciaPopular, Long> {

    /** Elenco filtrado por indicação (HIPERTENSAO, DIABETES...). */
    Page<MedicamentoFarmaciaPopular> findByIndicacao(IndicacaoFarmaciaPopular indicacao, Pageable pageable);
}
