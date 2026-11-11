package br.com.fiap.direitofacil.service;

import br.com.fiap.direitofacil.dto.response.ConsultaFarmaciaPopularResponse;
import br.com.fiap.direitofacil.dto.response.MedicamentoFarmaciaPopularResponse;
import br.com.fiap.direitofacil.enums.IndicacaoFarmaciaPopular;
import br.com.fiap.direitofacil.enums.ResultadoFarmaciaPopular;
import br.com.fiap.direitofacil.repository.MedicamentoFarmaciaPopularRepository;
import br.com.fiap.direitofacil.repository.PlSqlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Elenco do Farmácia Popular e consulta rápida de um remédio; quem decide é a PKG_FARMACIA_POPULAR. */
@Service
@RequiredArgsConstructor
public class FarmaciaPopularService {

    private final MedicamentoFarmaciaPopularRepository repository;
    private final PlSqlRepository plSqlRepository;

    @Transactional(readOnly = true)
    public Page<MedicamentoFarmaciaPopularResponse> listarElenco(IndicacaoFarmaciaPopular indicacao, Pageable pageable) {
        return (indicacao == null ? repository.findAll(pageable) : repository.findByIndicacao(indicacao, pageable))
                .map(DtoMapper::toResponse);
    }

    /** Mesma regra do item da receita (sem acento, sem maiúsculas, vírgula = ponto), mas sem gravar. */
    @Transactional(readOnly = true)
    public ConsultaFarmaciaPopularResponse consultar(String principioAtivo, String concentracao) {
        String resultado = plSqlRepository.consultarFarmaciaPopular(principioAtivo, concentracao);
        return new ConsultaFarmaciaPopularResponse(principioAtivo, concentracao, ResultadoFarmaciaPopular.valueOf(resultado));
    }
}
