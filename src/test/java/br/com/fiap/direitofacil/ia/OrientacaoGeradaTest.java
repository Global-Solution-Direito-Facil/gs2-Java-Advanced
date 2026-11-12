package br.com.fiap.direitofacil.ia;

import br.com.fiap.direitofacil.dto.request.RegistrarOrientacaoRequest;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** O passo a passo vai para o banco com um passo por linha, a fonte dos trechos e dentro do limite das colunas. */
class OrientacaoGeradaTest {

    private static final String FONTE = "Lei 8.742/1993 (LOAS), art. 20";

    @Test
    void cadaPassoViraUmaLinha() {
        OrientacaoGerada gerada = new OrientacaoGerada(true, List.of(" Atualize o Cadastro Único no CRAS. ", "", "Peça pelo\nMeu INSS."));

        RegistrarOrientacaoRequest request = gerada.paraRequest(FONTE, "gemini-2.5-flash");

        assertThat(request.passoAPasso()).isEqualTo("Atualize o Cadastro Único no CRAS.\nPeça pelo Meu INSS.");
        assertThat(request.fonte()).isEqualTo(FONTE);
        assertThat(request.modelo()).isEqualTo("gemini-2.5-flash");
    }

    @Test
    void semTrechoDoDireitoNaoGeraOrientacao() {
        assertThatThrownBy(() -> new OrientacaoGerada(false, List.of("Passo inventado.")).paraRequest(FONTE, "m"))
                .isInstanceOf(IaLeituraException.class);
        assertThatThrownBy(() -> new OrientacaoGerada(true, List.of()).paraRequest(FONTE, "m"))
                .isInstanceOf(IaLeituraException.class);
    }

    @Test
    void semFonteNaoGeraOrientacao() {
        assertThatThrownBy(() -> new OrientacaoGerada(true, List.of("Um passo.")).paraRequest(" ", "m"))
                .isInstanceOf(IaLeituraException.class)
                .hasMessageContaining("fonte");
    }

    @Test
    void respeitaOLimiteDeBytesDasColunas() {
        OrientacaoGerada gerada = new OrientacaoGerada(true, Collections.nCopies(40, "ç".repeat(100)));

        RegistrarOrientacaoRequest request = gerada.paraRequest("é".repeat(400), "m");

        assertThat(request.passoAPasso().getBytes(StandardCharsets.UTF_8).length).isLessThanOrEqualTo(4000);
        assertThat(request.passoAPasso().lines()).hasSize(19);
        assertThat(request.fonte().getBytes(StandardCharsets.UTF_8).length).isEqualTo(500);
    }
}
