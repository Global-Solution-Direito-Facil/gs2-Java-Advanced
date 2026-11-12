package br.com.fiap.direitofacil.ia;

import br.com.fiap.direitofacil.dto.response.MedicamentoLidoResponse;
import br.com.fiap.direitofacil.ia.ReceitaLida.Medicamento;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** A leitura da receita devolve só o que foi lido, limpo, para a tela de conferência. */
class ReceitaLidaTest {

    @Test
    void remediosLidosVaoParaATelaDeConferencia() {
        ReceitaLida lida = new ReceitaLida(true, List.of(
                new Medicamento(" Losartana Potássica ", "losartana potássica", " 50 mg "),
                new Medicamento("Glifage", null, null)), "");

        List<MedicamentoLidoResponse> remedios = lida.paraResponse();

        assertThat(remedios).containsExactly(
                new MedicamentoLidoResponse("Losartana Potássica", "losartana potássica", "50 mg"),
                new MedicamentoLidoResponse("Glifage", "", ""));
    }

    @Test
    void itensVaziosSaoDescartados() {
        List<Medicamento> itens = new ArrayList<>();
        itens.add(null);
        itens.add(new Medicamento(" ", "", "10 mg"));
        itens.add(new Medicamento("", "metformina", "850 mg"));

        assertThat(new ReceitaLida(true, itens, null).paraResponse())
                .containsExactly(new MedicamentoLidoResponse("", "metformina", "850 mg"));
    }

    @Test
    void receitaIlegivelDevolveOMotivoDaIa() {
        assertThatThrownBy(() -> new ReceitaLida(false, null, "A foto está desfocada.").paraResponse())
                .isInstanceOfSatisfying(IaLeituraException.class, e -> {
                    assertThat(e).hasMessage("A foto está desfocada.");
                    assertThat(e.isIndisponivel()).isFalse();
                });
    }

    @Test
    void receitaSemRemedioNaoDevolveListaVazia() {
        assertThatThrownBy(() -> new ReceitaLida(true, List.of(), null).paraResponse())
                .isInstanceOf(IaLeituraException.class)
                .hasMessageContaining("Nenhum remédio");
    }
}
