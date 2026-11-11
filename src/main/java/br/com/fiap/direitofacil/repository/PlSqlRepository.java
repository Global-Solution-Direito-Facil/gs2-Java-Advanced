package br.com.fiap.direitofacil.repository;

import br.com.fiap.direitofacil.enums.ResultadoElegibilidade;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Chamadas aos packages PL/SQL da disciplina de Database. A regra de
 * elegibilidade e o fluxo de status do caso vivem no Oracle; o Java só invoca.
 */
@Repository
public class PlSqlRepository {

    public record Avaliacao(ResultadoElegibilidade resultado, String motivo) {}

    private final SimpleJdbcCall avaliarDireito;
    private final SimpleJdbcCall avaliarElegibilidade;
    private final SimpleJdbcCall abrirCasos;
    private final SimpleJdbcCall reavaliarCasos;
    private final SimpleJdbcCall atualizarStatusCaso;
    private final SimpleJdbcCall registrarOrientacao;

    public PlSqlRepository(DataSource dataSource) {
        this.avaliarDireito = new SimpleJdbcCall(dataSource)
                .withCatalogName("PKG_ELEGIBILIDADE")
                .withProcedureName("PRC_AVALIAR_DIREITO");

        this.avaliarElegibilidade = new SimpleJdbcCall(dataSource)
                .withCatalogName("PKG_ELEGIBILIDADE")
                .withFunctionName("FN_AVALIAR_ELEGIBILIDADE");

        this.abrirCasos = new SimpleJdbcCall(dataSource)
                .withCatalogName("PKG_ELEGIBILIDADE")
                .withProcedureName("PRC_ABRIR_CASOS");

        this.reavaliarCasos = new SimpleJdbcCall(dataSource)
                .withCatalogName("PKG_ELEGIBILIDADE")
                .withProcedureName("PRC_REAVALIAR_CASOS");

        this.atualizarStatusCaso = new SimpleJdbcCall(dataSource)
                .withCatalogName("PKG_CASOS")
                .withProcedureName("PRC_ATUALIZAR_STATUS_CASO");

        this.registrarOrientacao = new SimpleJdbcCall(dataSource)
                .withCatalogName("PKG_CASOS")
                .withProcedureName("PRC_REGISTRAR_ORIENTACAO");
    }

    /** Avalia um direito para uma pessoa e devolve resultado e motivo, inclusive NAO_ELEGIVEL. */
    public Avaliacao avaliarDireito(Long pessoaId, Long direitoId, LocalDateTime dataReferencia) {
        Map<String, Object> saida = avaliarDireito.execute(Map.of(
                "P_ID_PESSOA", pessoaId,
                "P_ID_DIREITO", direitoId,
                "P_DT_REF", Timestamp.valueOf(dataReferencia)
        ));
        return new Avaliacao(
                ResultadoElegibilidade.valueOf((String) saida.get("P_STATUS")),
                (String) saida.get("P_MOTIVO")
        );
    }

    /** Veredito de um direito pelo código, sem o motivo. */
    public ResultadoElegibilidade avaliarElegibilidade(Long pessoaId, String codigoDireito, LocalDateTime dataReferencia) {
        String status = avaliarElegibilidade.executeFunction(String.class, Map.of(
                "P_ID_PESSOA", pessoaId,
                "P_COD_DIREITO", codigoDireito,
                "P_DT_REF", Timestamp.valueOf(dataReferencia)
        ));
        return ResultadoElegibilidade.valueOf(status);
    }

    /** Percorre o catálogo e abre um caso para cada direito ELEGIVEL ou POSSIVEL, sem duplicar. */
    public void abrirCasos(Long pessoaId, LocalDateTime dataReferencia) {
        abrirCasos.execute(Map.of(
                "P_ID_PESSOA", pessoaId,
                "P_DT_REF", Timestamp.valueOf(dataReferencia)
        ));
    }

    /**
     * Reavalia a pessoa depois que a renda ou os laudos mudam: atualiza os casos ABERTO,
     * remove os ABERTO que deixaram de ser elegíveis e abre os novos. Outros status não mudam.
     */
    public void reavaliarCasos(Long pessoaId, LocalDateTime dataReferencia) {
        reavaliarCasos.execute(Map.of(
                "P_ID_PESSOA", pessoaId,
                "P_DT_REF", Timestamp.valueOf(dataReferencia)
        ));
    }

    /** Muda o status pelo package, para a trigger validar o fluxo e gravar a auditoria. */
    public void atualizarStatusCaso(Long casoId, String novoStatus, LocalDateTime dataReferencia) {
        atualizarStatusCaso.execute(Map.of(
                "P_ID_CASO", casoId,
                "P_NOVO_STATUS", novoStatus,
                "P_DT_REF", Timestamp.valueOf(dataReferencia)
        ));
    }

    /** Grava ou substitui a orientação de um caso. */
    public void registrarOrientacao(Long casoId, String passoAPasso, String fonte, String modelo) {
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("P_ID_CASO", casoId);
        parametros.put("P_PASSO_A_PASSO", passoAPasso);
        parametros.put("P_FONTE", fonte);
        parametros.put("P_MODELO", modelo);
        registrarOrientacao.execute(parametros);
    }
}
