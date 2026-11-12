package br.com.fiap.direitofacil.ia;

import br.com.fiap.direitofacil.dto.request.LaudoRequest;
import br.com.fiap.direitofacil.enums.TipoCondicao;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** A leitura da IA só vira laudo quando traz os dados obrigatórios; nada é completado por conta própria. */
class LaudoLidoTest {

    @Test
    void leituraCompletaViraRegistroDoLaudo() {
        LaudoLido lido = new LaudoLido(true, " f84.0 ", " Transtorno do espectro autista ", TipoCondicao.TEA, "2026-03-10", "");

        LaudoRequest request = lido.paraRequest(7L, 3L);

        assertThat(request.pessoaId()).isEqualTo(7L);
        assertThat(request.documentoId()).isEqualTo(3L);
        assertThat(request.codCid()).isEqualTo("F84.0");
        assertThat(request.condicao()).isEqualTo("Transtorno do espectro autista");
        assertThat(request.tipoCondicao()).isEqualTo(TipoCondicao.TEA);
        assertThat(request.dataLaudo()).isEqualTo(LocalDate.of(2026, 3, 10));
    }

    @Test
    void tipoNaoClassificadoViraOutraECondicaoLongaECortada() {
        LaudoLido lido = new LaudoLido(true, "M54", "x".repeat(300), null, "2026-01-05", null);

        LaudoRequest request = lido.paraRequest(1L, null);

        assertThat(request.tipoCondicao()).isEqualTo(TipoCondicao.OUTRA);
        assertThat(request.condicao()).hasSize(200);
    }

    @Test
    void arquivoIlegivelDevolveOMotivoDaIa() {
        LaudoLido lido = new LaudoLido(false, null, null, null, null, "A imagem é uma conta de luz.");

        assertThatThrownBy(() -> lido.paraRequest(1L, null))
                .isInstanceOf(IaLeituraException.class)
                .hasMessage("A imagem é uma conta de luz.");
    }

    @Test
    void semCidSemCondicaoOuSemDataNaoViraLaudo() {
        assertThatThrownBy(() -> new LaudoLido(true, " ", "Autismo", TipoCondicao.TEA, "2026-03-10", null).paraRequest(1L, null))
                .isInstanceOf(IaLeituraException.class).hasMessageContaining("CID");
        assertThatThrownBy(() -> new LaudoLido(true, "F84.0", "", TipoCondicao.TEA, "2026-03-10", null).paraRequest(1L, null))
                .isInstanceOf(IaLeituraException.class).hasMessageContaining("condição");
        assertThatThrownBy(() -> new LaudoLido(true, "F84.0", "Autismo", TipoCondicao.TEA, "", null).paraRequest(1L, null))
                .isInstanceOf(IaLeituraException.class).hasMessageContaining("data");
    }

    @Test
    void dataInvalidaOuFuturaNaoViraLaudo() {
        assertThatThrownBy(() -> new LaudoLido(true, "F84.0", "Autismo", TipoCondicao.TEA, "10/03/2026", null).paraRequest(1L, null))
                .isInstanceOf(IaLeituraException.class);
        String amanha = LocalDate.now().plusDays(1).toString();
        assertThatThrownBy(() -> new LaudoLido(true, "F84.0", "Autismo", TipoCondicao.TEA, amanha, null).paraRequest(1L, null))
                .isInstanceOf(IaLeituraException.class).hasMessageContaining("futuro");
    }
}
