package br.com.fiap.direitofacil.service;

import br.com.fiap.direitofacil.auth.CurrentUser;
import br.com.fiap.direitofacil.dto.request.LaudoRequest;
import br.com.fiap.direitofacil.entity.Documento;
import br.com.fiap.direitofacil.entity.Pessoa;
import br.com.fiap.direitofacil.enums.StatusDocumento;
import br.com.fiap.direitofacil.enums.StatusDocumentoDotnet;
import br.com.fiap.direitofacil.enums.TipoCondicao;
import br.com.fiap.direitofacil.ia.IaLaudoService;
import br.com.fiap.direitofacil.ia.LaudoLido;
import br.com.fiap.direitofacil.ia.IaLeituraException;
import br.com.fiap.direitofacil.integracao.ArquivoBaixado;
import br.com.fiap.direitofacil.integracao.DocumentosDotnetClient;
import br.com.fiap.direitofacil.mensageria.DocumentoRecebido;
import br.com.fiap.direitofacil.repository.DocumentoRepository;
import br.com.fiap.direitofacil.repository.PessoaRepository;
import br.com.fiap.direitofacil.service.ProcessamentoDocumentoService.Resultado;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/** O caminho do laudo que chega pela fila, sem banco, sem IA e sem a .NET de verdade. */
@ExtendWith(MockitoExtension.class)
class ProcessamentoDocumentoServiceTest {

    private static final String DOCUMENTO = "3f2504e0-4f89-11d3-9a0c-0305e82c3301";
    private static final String EMAIL = "ana.almeida@email.com";

    @Mock private DocumentoRepository documentoRepository;
    @Mock private PessoaRepository pessoaRepository;
    @Mock private DocumentosDotnetClient dotnetClient;
    @Mock private IaLaudoService iaLaudoService;
    @Mock private LaudoService laudoService;
    @Mock private DocumentoService documentoService;

    @InjectMocks private ProcessamentoDocumentoService service;

    private final ArquivoBaixado arquivo = new ArquivoBaixado(new byte[]{1, 2, 3}, "application/pdf", "laudo.pdf");
    private final LaudoLido leitura = new LaudoLido(true, "F84.0", "Transtorno do espectro autista", TipoCondicao.TEA, "2026-03-10", "");

    private DocumentoRecebido evento(String tipo, Long pessoaId) {
        return new DocumentoRecebido("evento-1", DOCUMENTO, EMAIL, pessoaId, tipo, "application/pdf", "2026-11-11T10:00:00Z");
    }

    private void documentoNovoSalvoComId(long id) {
        when(documentoRepository.findByIdExterno(DOCUMENTO)).thenReturn(Optional.empty());
        when(pessoaRepository.getReferenceById(2L)).thenReturn(Pessoa.builder().id(2L).build());
        when(documentoRepository.saveAndFlush(any(Documento.class))).thenAnswer(chamada -> {
            Documento salvo = chamada.getArgument(0);
            salvo.setId(id);
            return salvo;
        });
    }

    @Test
    void laudoLidoGravaOLaudoComPermissaoInternaEAvisaADotnet() {
        documentoNovoSalvoComId(9L);
        when(pessoaRepository.existsByIdAndUsuarioEmailIgnoreCase(2L, EMAIL)).thenReturn(true);
        when(dotnetClient.baixarArquivo(DOCUMENTO)).thenReturn(arquivo);
        when(iaLaudoService.ler(arquivo)).thenReturn(leitura);
        AtomicBoolean gravouComoGestor = new AtomicBoolean();
        when(laudoService.registrar(any())).thenAnswer(chamada -> {
            gravouComoGestor.set(CurrentUser.isGestor());
            return null;
        });

        Resultado resultado = service.processar(evento("Laudo", 2L));

        assertThat(resultado).isEqualTo(Resultado.EXTRAIDO);
        ArgumentCaptor<LaudoRequest> request = ArgumentCaptor.forClass(LaudoRequest.class);
        verify(laudoService).registrar(request.capture());
        assertThat(request.getValue().pessoaId()).isEqualTo(2L);
        assertThat(request.getValue().documentoId()).isEqualTo(9L);
        assertThat(request.getValue().codCid()).isEqualTo("F84.0");
        assertThat(gravouComoGestor).isTrue();
        assertThat(CurrentUser.isGestor()).isFalse();

        ArgumentCaptor<Documento> documento = ArgumentCaptor.forClass(Documento.class);
        verify(documentoRepository).saveAndFlush(documento.capture());
        assertThat(documento.getValue().getIdExterno()).isEqualTo(DOCUMENTO);
        assertThat(documento.getValue().getChaveIdempotencia()).isEqualTo("evento-1");
        assertThat(documento.getValue().getTamanhoBytes()).isEqualTo(3L);
        verify(dotnetClient).informarStatus(DOCUMENTO, StatusDocumentoDotnet.EXTRAIDO);
    }

    @Test
    void semPessoaIdUsaOUnicoBeneficiarioDaConta() {
        documentoNovoSalvoComId(9L);
        when(pessoaRepository.findTop2ByUsuarioEmailIgnoreCaseOrderByIdAsc(EMAIL))
                .thenReturn(List.of(Pessoa.builder().id(2L).build()));
        when(dotnetClient.baixarArquivo(DOCUMENTO)).thenReturn(arquivo);
        when(iaLaudoService.ler(arquivo)).thenReturn(leitura);

        assertThat(service.processar(evento("LAUDO", null))).isEqualTo(Resultado.EXTRAIDO);
    }

    @Test
    void semPessoaIdEComDoisBeneficiariosNaoAdivinha() {
        when(documentoRepository.findByIdExterno(DOCUMENTO)).thenReturn(Optional.empty());
        when(pessoaRepository.findTop2ByUsuarioEmailIgnoreCaseOrderByIdAsc(EMAIL))
                .thenReturn(List.of(Pessoa.builder().id(2L).build(), Pessoa.builder().id(3L).build()));

        assertThat(service.processar(evento("LAUDO", null))).isEqualTo(Resultado.SEM_BENEFICIARIO);

        verify(dotnetClient).informarStatus(DOCUMENTO, StatusDocumentoDotnet.FALHA);
        verify(dotnetClient, never()).baixarArquivo(any());
        verifyNoInteractions(iaLaudoService, laudoService);
    }

    @Test
    void pessoaDeOutraContaERecusada() {
        when(documentoRepository.findByIdExterno(DOCUMENTO)).thenReturn(Optional.empty());
        when(pessoaRepository.existsByIdAndUsuarioEmailIgnoreCase(5L, EMAIL)).thenReturn(false);

        assertThat(service.processar(evento("LAUDO", 5L))).isEqualTo(Resultado.SEM_BENEFICIARIO);

        verifyNoInteractions(iaLaudoService, laudoService);
    }

    @Test
    void leituraQueFalhaMarcaErroAquiEFalhaNaDotnet() {
        documentoNovoSalvoComId(9L);
        when(pessoaRepository.existsByIdAndUsuarioEmailIgnoreCase(2L, EMAIL)).thenReturn(true);
        when(dotnetClient.baixarArquivo(DOCUMENTO)).thenReturn(arquivo);
        when(iaLaudoService.ler(arquivo)).thenThrow(new IaLeituraException("ilegível"));

        assertThat(service.processar(evento("LAUDO", 2L))).isEqualTo(Resultado.FALHA_NA_LEITURA);

        verify(documentoService).marcarErro(9L);
        verify(dotnetClient).informarStatus(DOCUMENTO, StatusDocumentoDotnet.FALHA);
        verifyNoInteractions(laudoService);
    }

    @Test
    void arquivoIndisponivelNaoGravaNada() {
        when(documentoRepository.findByIdExterno(DOCUMENTO)).thenReturn(Optional.empty());
        when(pessoaRepository.existsByIdAndUsuarioEmailIgnoreCase(2L, EMAIL)).thenReturn(true);
        when(dotnetClient.baixarArquivo(DOCUMENTO)).thenThrow(new ResponseStatusException(HttpStatus.BAD_GATEWAY, "fora"));

        assertThat(service.processar(evento("LAUDO", 2L))).isEqualTo(Resultado.ARQUIVO_INDISPONIVEL);

        verify(documentoRepository, never()).saveAndFlush(any());
        verifyNoInteractions(iaLaudoService, laudoService);
    }

    @Test
    void eventoRepetidoDeDocumentoJaProcessadoNaoLeDeNovo() {
        Documento processado = Documento.builder().id(9L).idExterno(DOCUMENTO).status(StatusDocumento.PROCESSADO).build();
        when(documentoRepository.findByIdExterno(DOCUMENTO)).thenReturn(Optional.of(processado));

        assertThat(service.processar(evento("LAUDO", 2L))).isEqualTo(Resultado.JA_PROCESSADO);

        verify(dotnetClient).informarStatus(DOCUMENTO, StatusDocumentoDotnet.EXTRAIDO);
        verify(dotnetClient, never()).baixarArquivo(any());
        verifyNoInteractions(iaLaudoService, laudoService);
    }

    @Test
    void documentoQueNaoELaudoEIgnorado() {
        assertThat(service.processar(evento("RECEITA", 2L))).isEqualTo(Resultado.IGNORADO);

        verifyNoInteractions(documentoRepository, dotnetClient, iaLaudoService, laudoService);
    }
}
