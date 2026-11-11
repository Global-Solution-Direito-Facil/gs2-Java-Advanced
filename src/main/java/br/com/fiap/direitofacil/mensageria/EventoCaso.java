package br.com.fiap.direitofacil.mensageria;

import br.com.fiap.direitofacil.entity.Caso;

import java.time.LocalDateTime;

/** Corpo JSON dos eventos de caso (DireitoIdentificado, PrazoVencendo e CasoParado). */
public record EventoCaso(
        TipoEventoCaso tipo,
        Long casoId,
        Long pessoaId,
        String pessoaNome,
        String codigoDireito,
        String nomeDireito,
        String status,
        String resultadoAvaliacao,
        String motivo,
        LocalDateTime dataPrazo,
        LocalDateTime dataAtualizacao,
        LocalDateTime ocorridoEm
) {
    /** Monta o evento a partir do caso; chamar dentro da transação, porque pessoa e direito são LAZY. */
    public static EventoCaso de(TipoEventoCaso tipo, Caso caso) {
        return new EventoCaso(
                tipo,
                caso.getId(),
                caso.getPessoa().getId(),
                caso.getPessoa().getNome(),
                caso.getDireito().getCodigo(),
                caso.getDireito().getNome(),
                caso.getStatus().name(),
                caso.getResultadoAvaliacao().name(),
                caso.getMotivo(),
                caso.getDataPrazo(),
                caso.getDataAtualizacao(),
                LocalDateTime.now()
        );
    }
}
