package br.com.fiap.direitofacil.service;

import br.com.fiap.direitofacil.auth.ContextoInterno;
import br.com.fiap.direitofacil.dto.request.LaudoRequest;
import br.com.fiap.direitofacil.entity.Documento;
import br.com.fiap.direitofacil.entity.Pessoa;
import br.com.fiap.direitofacil.enums.StatusDocumento;
import br.com.fiap.direitofacil.enums.StatusDocumentoDotnet;
import br.com.fiap.direitofacil.ia.IaLaudoService;
import br.com.fiap.direitofacil.ia.LaudoLido;
import br.com.fiap.direitofacil.ia.IaLeituraException;
import br.com.fiap.direitofacil.integracao.ArquivoBaixado;
import br.com.fiap.direitofacil.integracao.DocumentosDotnetClient;
import br.com.fiap.direitofacil.mensageria.DocumentoRecebido;
import br.com.fiap.direitofacil.repository.DocumentoRepository;
import br.com.fiap.direitofacil.repository.PessoaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

/**
 * O caminho do laudo que chega pela fila: acha o beneficiário, baixa o arquivo da .NET, pede a
 * leitura à IA, grava o laudo (o que dispara a regra PL/SQL e abre os casos) e avisa a .NET.
 * Não é transacional de propósito: a chamada à IA demora e não pode segurar uma conexão do banco.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessamentoDocumentoService {

    public enum Resultado { IGNORADO, JA_PROCESSADO, SEM_BENEFICIARIO, ARQUIVO_INDISPONIVEL, FALHA_NA_LEITURA, EXTRAIDO }

    private static final int TAMANHO_NOME_ARQUIVO = 200;
    private static final int TAMANHO_TIPO_MIDIA = 50;

    private final DocumentoRepository documentoRepository;
    private final PessoaRepository pessoaRepository;
    private final DocumentosDotnetClient dotnetClient;
    private final IaLaudoService iaLaudoService;
    private final LaudoService laudoService;
    private final DocumentoService documentoService;

    /** Processa um DocumentoRecebido; o mesmo evento entregue de novo não gera um segundo laudo. */
    public Resultado processar(DocumentoRecebido evento) {
        if (evento.documentoId() == null || evento.documentoId().isBlank() || !evento.ehLaudo()) {
            log.info("Documento {} do tipo {} não passa pela leitura de laudo", evento.documentoId(), evento.tipoDocumento());
            return Resultado.IGNORADO;
        }

        Optional<Documento> existente = documentoRepository.findByIdExterno(evento.documentoId());
        if (existente.isPresent() && existente.get().getStatus() == StatusDocumento.PROCESSADO) {
            dotnetClient.informarStatus(evento.documentoId(), StatusDocumentoDotnet.EXTRAIDO);
            return Resultado.JA_PROCESSADO;
        }

        Long pessoaId = existente.map(documento -> documento.getPessoa().getId())
                .or(() -> beneficiario(evento))
                .orElse(null);
        if (pessoaId == null) {
            log.warn("Documento {}: não foi possível identificar o beneficiário de {}", evento.documentoId(), evento.cidadaoId());
            dotnetClient.informarStatus(evento.documentoId(), StatusDocumentoDotnet.FALHA);
            return Resultado.SEM_BENEFICIARIO;
        }

        ArquivoBaixado arquivo;
        try {
            arquivo = dotnetClient.baixarArquivo(evento.documentoId());
        } catch (ResponseStatusException e) {
            log.warn("Documento {}: arquivo indisponível ({})", evento.documentoId(), e.getReason());
            return Resultado.ARQUIVO_INDISPONIVEL;
        }

        Long documentoId = existente.map(Documento::getId)
                .orElseGet(() -> registrar(evento, pessoaId, arquivo));

        try {
            LaudoLido lido = iaLaudoService.ler(arquivo);
            LaudoRequest request = lido.paraRequest(pessoaId, documentoId);
            ContextoInterno.executar(() -> laudoService.registrar(request));
        } catch (IaLeituraException | ResponseStatusException e) {
            log.warn("Documento {}: leitura do laudo falhou: {}", evento.documentoId(), e.getMessage());
            documentoService.marcarErro(documentoId);
            dotnetClient.informarStatus(evento.documentoId(), StatusDocumentoDotnet.FALHA);
            return Resultado.FALHA_NA_LEITURA;
        }

        dotnetClient.informarStatus(evento.documentoId(), StatusDocumentoDotnet.EXTRAIDO);
        return Resultado.EXTRAIDO;
    }

    /**
     * O beneficiário vem do evento (conferindo que é da conta que enviou). Sem esse campo, vale
     * o único beneficiário da conta; com mais de um, não dá para saber de quem é o laudo.
     */
    private Optional<Long> beneficiario(DocumentoRecebido evento) {
        String email = evento.cidadaoId() == null ? "" : evento.cidadaoId().strip();
        if (email.isEmpty()) {
            return Optional.empty();
        }
        if (evento.pessoaId() != null) {
            return pessoaRepository.existsByIdAndUsuarioEmailIgnoreCase(evento.pessoaId(), email)
                    ? Optional.of(evento.pessoaId())
                    : Optional.empty();
        }
        List<Pessoa> pessoas = pessoaRepository.findTop2ByUsuarioEmailIgnoreCaseOrderByIdAsc(email);
        return pessoas.size() == 1 ? Optional.of(pessoas.get(0).getId()) : Optional.empty();
    }

    /** Grava o registro do documento neste banco; a chave de idempotência é o id do evento. */
    private Long registrar(DocumentoRecebido evento, Long pessoaId, ArquivoBaixado arquivo) {
        String chave = evento.eventId() == null || evento.eventId().isBlank() ? evento.documentoId() : evento.eventId();
        try {
            return documentoRepository.saveAndFlush(Documento.builder()
                    .pessoa(pessoaRepository.getReferenceById(pessoaId))
                    .idExterno(evento.documentoId())
                    .nomeArquivo(cortar(arquivo.nomeArquivo(), TAMANHO_NOME_ARQUIVO))
                    .tipoMidia(cortar(arquivo.tipoMidia(), TAMANHO_TIPO_MIDIA))
                    .tamanhoBytes(arquivo.tamanhoBytes())
                    .chaveIdempotencia(chave)
                    .build()).getId();
        } catch (DataIntegrityViolationException e) {
            return documentoRepository.findByIdExterno(evento.documentoId())
                    .map(Documento::getId)
                    .orElseThrow(() -> e);
        }
    }

    private String cortar(String texto, int tamanho) {
        return texto.length() <= tamanho ? texto : texto.substring(0, tamanho);
    }
}
