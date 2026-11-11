package br.com.fiap.direitofacil.service;

import br.com.fiap.direitofacil.dto.response.AvaliacaoDireitoResponse;
import br.com.fiap.direitofacil.entity.Direito;
import br.com.fiap.direitofacil.repository.PlSqlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ElegibilidadeService {

    private final PlSqlRepository plSqlRepository;
    private final PessoaService pessoaService;
    private final DireitoService direitoService;

    /** Avalia todo o catálogo para a pessoa na hora, via PKG_ELEGIBILIDADE, incluindo os negativos com motivo. */
    @Transactional(readOnly = true)
    public List<AvaliacaoDireitoResponse> avaliarTodos(Long pessoaId) {
        pessoaService.findEntityByIdAutorizado(pessoaId);
        LocalDateTime agora = LocalDateTime.now();
        return direitoService.listarTodos().stream()
                .map(direito -> avaliar(pessoaId, direito, agora))
                .toList();
    }

    /** Pede ao Oracle para abrir um caso para cada direito ELEGIVEL ou POSSIVEL da pessoa, sem duplicar. */
    @Transactional
    public void abrirCasos(Long pessoaId) {
        pessoaService.findEntityByIdAutorizado(pessoaId);
        plSqlRepository.abrirCasos(pessoaId, LocalDateTime.now());
    }

    /** Chama a procedure para um direito e monta o response com o resumo do direito. */
    private AvaliacaoDireitoResponse avaliar(Long pessoaId, Direito direito, LocalDateTime dataReferencia) {
        PlSqlRepository.Avaliacao avaliacao = plSqlRepository.avaliarDireito(pessoaId, direito.getId(), dataReferencia);
        return new AvaliacaoDireitoResponse(DtoMapper.toResumo(direito), avaliacao.resultado(), avaliacao.motivo());
    }
}
