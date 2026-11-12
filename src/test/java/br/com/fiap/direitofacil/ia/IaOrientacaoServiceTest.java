package br.com.fiap.direitofacil.ia;

import br.com.fiap.direitofacil.auth.CurrentUser;
import br.com.fiap.direitofacil.dto.request.RegistrarOrientacaoRequest;
import br.com.fiap.direitofacil.dto.response.CasoResponse;
import br.com.fiap.direitofacil.dto.response.DireitoResumoResponse;
import br.com.fiap.direitofacil.dto.response.OrientacaoResponse;
import br.com.fiap.direitofacil.service.CasoService;
import br.com.fiap.direitofacil.service.OrientacaoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/** O RAG da orientação sem Gemini: quais trechos entram, de onde vem a fonte e quando nada é gravado. */
@ExtendWith(MockitoExtension.class)
class IaOrientacaoServiceTest {

    private static final DireitoResumoResponse BPC_PCD =
            new DireitoResumoResponse(2L, "BPC_PCD", "BPC/LOAS - Pessoa com Deficiencia", "INSS", "Lei 8.742/1993 art. 20");

    @Mock private IaBaseConhecimento baseConhecimento;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS) private ChatClient chatClient;
    @Mock private CasoService casoService;
    @Mock private OrientacaoService orientacaoService;

    private IaOrientacaoService service;

    @BeforeEach
    void montar() {
        service = new IaOrientacaoService(baseConhecimento, chatClient, casoService, orientacaoService, "gemini-2.5-flash");
    }

    private CasoResponse caso(OrientacaoResponse orientacao) {
        return new CasoResponse(7L, null, BPC_PCD, null, null, null, null, null, null, orientacao);
    }

    private Document trecho(String codigo, String fonte) {
        return new Document("texto de " + codigo, Map.of(IaBaseConhecimento.CODIGO, codigo, IaBaseConhecimento.FONTE, fonte));
    }

    @SuppressWarnings("unchecked")
    private void iaResponde(OrientacaoGerada gerada) {
        when(chatClient.prompt().system(anyString()).user(any(Consumer.class)).call().entity(OrientacaoGerada.class))
                .thenReturn(gerada);
    }

    @Test
    void usaSoOTrechoDoMesmoDireitoEACitacaoVemDele() {
        when(casoService.buscarPorId(7L)).thenReturn(caso(null));
        when(baseConhecimento.buscar(anyString(), anyInt())).thenReturn(List.of(
                trecho("BPC_IDOSO", "fonte do idoso"), trecho("BPC_PCD", "Lei 8.742/1993 (LOAS), art. 20")));
        iaResponde(new OrientacaoGerada(true, List.of("Atualize o Cadastro Único.", "Peça pelo Meu INSS.")));
        AtomicBoolean gravouComoGestor = new AtomicBoolean();
        when(orientacaoService.registrar(eq(7L), any())).thenAnswer(chamada -> {
            gravouComoGestor.set(CurrentUser.isGestor());
            return new OrientacaoResponse(1L, "", "", "", LocalDateTime.now());
        });

        service.gerar(7L);

        ArgumentCaptor<RegistrarOrientacaoRequest> request = ArgumentCaptor.forClass(RegistrarOrientacaoRequest.class);
        verify(orientacaoService).registrar(eq(7L), request.capture());
        assertThat(request.getValue().passoAPasso()).isEqualTo("Atualize o Cadastro Único.\nPeça pelo Meu INSS.");
        assertThat(request.getValue().fonte()).isEqualTo("Lei 8.742/1993 (LOAS), art. 20");
        assertThat(request.getValue().modelo()).isEqualTo("gemini-2.5-flash");
        assertThat(gravouComoGestor).isTrue();
        assertThat(CurrentUser.isGestor()).isFalse();
    }

    @Test
    void quandoAIaDizQueOsTrechosNaoSaoDoDireitoNadaEGravado() {
        when(casoService.buscarPorId(7L)).thenReturn(caso(null));
        when(baseConhecimento.buscar(anyString(), anyInt())).thenReturn(List.of(trecho("CIPTEA", "outra fonte")));
        iaResponde(new OrientacaoGerada(false, List.of()));

        assertThatThrownBy(() -> service.gerar(7L))
                .isInstanceOfSatisfying(IaLeituraException.class, e -> assertThat(e.isIndisponivel()).isFalse());

        verify(orientacaoService, never()).registrar(any(), any());
    }

    @Test
    void semTrechoNaBaseNaoChamaAIa() {
        when(casoService.buscarPorId(7L)).thenReturn(caso(null));
        when(baseConhecimento.buscar(anyString(), anyInt())).thenReturn(List.of());

        assertThatThrownBy(() -> service.gerar(7L)).isInstanceOf(IaLeituraException.class);

        verifyNoInteractions(chatClient, orientacaoService);
    }

    @Test
    void casoNovoQueJaTemOrientacaoNaoEGeradoDeNovo() {
        when(casoService.buscarPorId(7L)).thenReturn(caso(new OrientacaoResponse(1L, "passo", "fonte", "m", LocalDateTime.now())));

        assertThat(service.gerarParaCasoNovo(7L)).isFalse();

        verifyNoInteractions(baseConhecimento, chatClient, orientacaoService);
    }
}
