package br.com.fiap.diretofacil.service;

import br.com.fiap.diretofacil.dto.response.*;
import br.com.fiap.diretofacil.entity.*;

/** Converte entidades nos records de resposta da API. */
public final class DtoMapper {

    private DtoMapper() {}

    public static UsuarioResponse toResponse(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getCpf(),
                usuario.getEmail(),
                usuario.getPerfil(),
                usuario.getDataCadastro()
        );
    }

    public static UsuarioResumoResponse toResumo(Usuario usuario) {
        if (usuario == null) {
            return null;
        }
        return new UsuarioResumoResponse(usuario.getId(), usuario.getNome(), usuario.getEmail());
    }

    public static PessoaResponse toResponse(Pessoa pessoa) {
        return new PessoaResponse(
                pessoa.getId(),
                toResumo(pessoa.getUsuario()),
                pessoa.getNome(),
                pessoa.getDataNascimento(),
                pessoa.getIdade(),
                pessoa.getRendaFamiliar(),
                pessoa.getQtdMembrosFamilia(),
                pessoa.getRendaPerCapita(),
                pessoa.getDataCadastro()
        );
    }

    public static PessoaResumoResponse toResumo(Pessoa pessoa) {
        if (pessoa == null) {
            return null;
        }
        return new PessoaResumoResponse(pessoa.getId(), pessoa.getNome());
    }

    public static CriterioDireitoResponse toResponse(CriterioDireito criterio) {
        if (criterio == null) {
            return null;
        }
        return new CriterioDireitoResponse(
                criterio.getIdadeMin(),
                criterio.getIdadeMax(),
                criterio.getExigePcd(),
                criterio.getExigeTea(),
                criterio.getFracaoSmMax(),
                criterio.getPctTolerancia()
        );
    }

    public static DireitoResponse toResponse(Direito direito) {
        return new DireitoResponse(
                direito.getId(),
                direito.getCodigo(),
                direito.getNome(),
                direito.getOrgao(),
                direito.getBaseLegal(),
                direito.getGrupoAlvo(),
                direito.getPrazoDias(),
                direito.getStatusVerificacao(),
                toResponse(direito.getCriterio())
        );
    }

    public static DireitoResumoResponse toResumo(Direito direito) {
        if (direito == null) {
            return null;
        }
        return new DireitoResumoResponse(
                direito.getId(),
                direito.getCodigo(),
                direito.getNome(),
                direito.getOrgao(),
                direito.getBaseLegal()
        );
    }

    public static DocumentoResponse toResponse(Documento documento) {
        return new DocumentoResponse(
                documento.getId(),
                toResumo(documento.getPessoa()),
                documento.getIdExterno(),
                documento.getNomeArquivo(),
                documento.getTipoMidia(),
                documento.getTamanhoBytes(),
                documento.getStatus(),
                documento.getDataRecebimento(),
                documento.getDataProcessamento()
        );
    }

    public static LaudoResponse toResponse(Laudo laudo) {
        return new LaudoResponse(
                laudo.getId(),
                toResumo(laudo.getPessoa()),
                laudo.getDocumento() != null ? laudo.getDocumento().getId() : null,
                laudo.getCodCid(),
                laudo.getCondicao(),
                laudo.getTipoCondicao(),
                laudo.getDataLaudo(),
                laudo.getDataCadastro()
        );
    }

    public static OrientacaoResponse toResponse(Orientacao orientacao) {
        if (orientacao == null) {
            return null;
        }
        return new OrientacaoResponse(
                orientacao.getId(),
                orientacao.getPassoAPasso(),
                orientacao.getFonte(),
                orientacao.getModelo(),
                orientacao.getDataGeracao()
        );
    }

    public static CasoResponse toResponse(Caso caso) {
        return new CasoResponse(
                caso.getId(),
                toResumo(caso.getPessoa()),
                toResumo(caso.getDireito()),
                caso.getStatus(),
                caso.getResultadoAvaliacao(),
                caso.getMotivo(),
                caso.getDataAbertura(),
                caso.getDataPrazo(),
                caso.getDataAtualizacao(),
                toResponse(caso.getOrientacao())
        );
    }

    public static SalarioMinimoResponse toResponse(SalarioMinimo salarioMinimo) {
        return new SalarioMinimoResponse(
                salarioMinimo.getAnoReferencia(),
                salarioMinimo.getValor(),
                salarioMinimo.getFonte()
        );
    }
}
