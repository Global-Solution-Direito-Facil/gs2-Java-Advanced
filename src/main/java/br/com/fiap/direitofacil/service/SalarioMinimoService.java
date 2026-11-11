package br.com.fiap.direitofacil.service;

import br.com.fiap.direitofacil.dto.response.SalarioMinimoResponse;
import br.com.fiap.direitofacil.repository.SalarioMinimoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class SalarioMinimoService {

    private final SalarioMinimoRepository repository;

    /** Salário mínimo mais recente, o mesmo que a regra PL/SQL usa para calcular a renda per capita. */
    @Transactional(readOnly = true)
    public SalarioMinimoResponse atual() {
        return repository.findTopByOrderByAnoReferenciaDesc()
                .map(DtoMapper::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Salário mínimo não cadastrado."));
    }
}
