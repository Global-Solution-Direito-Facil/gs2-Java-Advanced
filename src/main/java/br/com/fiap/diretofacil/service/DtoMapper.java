package br.com.fiap.diretofacil.service;

import br.com.fiap.diretofacil.dto.response.*;
import br.com.fiap.diretofacil.entity.*;

public final class DtoMapper {

    private DtoMapper() {}

    public static CidadaoResponse toResponse(Cidadao cidadao, String email) {
        return new CidadaoResponse(
                cidadao.getId(),
                cidadao.getNome(),
                cidadao.getCpf(),
                cidadao.getDataNascimento(),
                cidadao.getIdade(),
                cidadao.getRendaFamiliar(),
                cidadao.getQtdPessoasFamilia(),
                cidadao.getRendaPerCapita(),
                cidadao.getCadunico(),
                cidadao.getMunicipio(),
                cidadao.getUf(),
                email,
                cidadao.getDataCadastro()
        );
    }

    public static CidadaoResumoResponse toResumo(Cidadao cidadao) {
        if (cidadao == null) {
            return null;
        }
        return new CidadaoResumoResponse(
                cidadao.getId(),
                cidadao.getNome(),
                cidadao.getCpf()
        );
    }

    public static GestorResponse toResponse(Gestor gestor, String email) {
        return new GestorResponse(
                gestor.getId(),
                gestor.getNome(),
                gestor.getCpf(),
                gestor.getMatricula(),
                gestor.getUnidade(),
                gestor.getTelefone(),
                gestor.getAtivo(),
                email,
                gestor.getDataCadastro()
        );
    }

    public static GestorResumoResponse toResumo(Gestor gestor) {
        if (gestor == null) {
            return null;
        }
        return new GestorResumoResponse(
                gestor.getId(),
                gestor.getNome(),
                gestor.getUnidade()
        );
    }

    public static DireitoResponse toResponse(Direito direito) {
        return new DireitoResponse(
                direito.getId(),
                direito.getCodigo(),
                direito.getNome(),
                direito.getCriterios(),
                direito.getOndePedir(),
                direito.getBaseLegal(),
                direito.getUrlBaseLegal(),
                direito.getPublicos(),
                direito.getRendaPerCapitaMaxSm(),
                direito.getIdadeMinima(),
                direito.getExigeCadunico(),
                direito.getExigeDeficiencia(),
                direito.getExigeTea(),
                direito.getStatusConferencia(),
                direito.getAtivo()
        );
    }

    public static DireitoResumoResponse toResumo(Direito direito) {
        if (direito == null) {
            return null;
        }
        return new DireitoResumoResponse(
                direito.getId(),
                direito.getCodigo(),
                direito.getNome()
        );
    }

    public static DocumentoResponse toResponse(Documento documento) {
        return new DocumentoResponse(
                documento.getId(),
                toResumo(documento.getCidadao()),
                documento.getTipo(),
                documento.getNomeArquivo(),
                documento.getContentType(),
                documento.getStatus(),
                documento.getCondicao(),
                documento.getCid(),
                documento.getIdadeExtraida(),
                documento.getConfiancaExtracao(),
                documento.getMensagemErro(),
                documento.getDataEnvio(),
                documento.getDataProcessamento()
        );
    }

    public static AvaliacaoDireitoResponse toResponse(AvaliacaoDireito avaliacao) {
        return new AvaliacaoDireitoResponse(
                avaliacao.getId(),
                toResumo(avaliacao.getDireito()),
                avaliacao.getResultado(),
                avaliacao.getMotivo(),
                avaliacao.getDataAvaliacao()
        );
    }

    public static CasoResponse toResponse(Caso caso) {
        return new CasoResponse(
                caso.getId(),
                toResumo(caso.getCidadao()),
                toResumo(caso.getDireito()),
                toResumo(caso.getGestorResponsavel()),
                caso.getStatus(),
                caso.getPrazo(),
                caso.getPassoAPasso(),
                caso.getFontes(),
                caso.getObservacaoGestor(),
                caso.getDataAbertura(),
                caso.getDataAtualizacao()
        );
    }
}
