package br.com.fiap.diretofacil.repository;

import br.com.fiap.diretofacil.entity.Documento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** Acesso aos documentos anunciados pelo evento DocumentoRecebido. */
public interface DocumentoRepository extends JpaRepository<Documento, Long> {

    /** Detecta o mesmo evento entregue duas vezes pela fila e devolve o registro já salvo. */
    Optional<Documento> findByChaveIdempotencia(String chaveIdempotencia);

    /** Localiza o documento pelo id dele na API .NET. */
    Optional<Documento> findByIdExterno(String idExterno);

    /** Documentos de um beneficiário, paginados. */
    Page<Documento> findByPessoaId(Long pessoaId, Pageable pageable);

    /** Documentos de todos os beneficiários de um responsável, para o cidadão ver os seus. */
    Page<Documento> findByPessoaUsuarioId(Long usuarioId, Pageable pageable);
}
