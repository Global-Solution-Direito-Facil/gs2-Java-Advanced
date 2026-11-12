package br.com.fiap.direitofacil.ia;

import br.com.fiap.direitofacil.auth.ContextoInterno;
import br.com.fiap.direitofacil.dto.response.MedicamentoLidoResponse;
import br.com.fiap.direitofacil.ia.ReceitaLida.Medicamento;
import br.com.fiap.direitofacil.integracao.ArquivoBaixado;
import br.com.fiap.direitofacil.integracao.DocumentosDotnetClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/** A extração da receita baixa o arquivo em nome de quem pediu e não chama a IA sem autenticação. */
@ExtendWith(MockitoExtension.class)
class IaReceitaServiceTest {

    private static final String DOCUMENTO = "3f2504e0-4f89-11d3-9a0c-0305e82c3301";

    @Mock private DocumentosDotnetClient dotnetClient;
    @Mock private IaLeitorDocumento leitor;

    @InjectMocks private IaReceitaService service;

    @Test
    void baixaComOTokenDeQuemPediuEDevolveOsRemedios() {
        ArquivoBaixado arquivo = new ArquivoBaixado(new byte[]{1}, "image/jpeg", "receita.jpg");
        when(dotnetClient.baixarArquivoComoUsuario(DOCUMENTO, "interno")).thenReturn(arquivo);
        when(leitor.ler(anyString(), anyString(), eq(arquivo), eq(ReceitaLida.class)))
                .thenReturn(new ReceitaLida(true, List.of(new Medicamento("Losartana", "losartana potássica", "50 mg")), ""));

        List<MedicamentoLidoResponse> remedios = ContextoInterno.executar(() -> service.extrair(DOCUMENTO));

        assertThat(remedios).containsExactly(new MedicamentoLidoResponse("Losartana", "losartana potássica", "50 mg"));
    }

    @Test
    void semAutenticacaoNaoBaixaNemChamaAIa() {
        assertThatThrownBy(() -> service.extrair(DOCUMENTO))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED));

        verifyNoInteractions(dotnetClient, leitor);
    }

    @Test
    void documentoDeOutraPessoaNaoChegaNaIa() {
        when(dotnetClient.baixarArquivoComoUsuario(any(), any()))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Documento não encontrado."));

        assertThatThrownBy(() -> ContextoInterno.executar(() -> service.extrair(DOCUMENTO)))
                .isInstanceOf(ResponseStatusException.class);

        verifyNoInteractions(leitor);
    }
}
