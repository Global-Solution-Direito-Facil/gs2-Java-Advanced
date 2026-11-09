package br.com.fiap.diretofacil.repository;

import br.com.fiap.diretofacil.entity.Cidadao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CidadaoRepository extends JpaRepository<Cidadao, Long> {

    /** Localiza o perfil pelo CPF; usado no registro para ligar a conta a um cidadão já cadastrado pelo gestor. */
    Optional<Cidadao> findByCpf(String cpf);

    /** Impede CPF duplicado ao criar um cidadão. */
    boolean existsByCpf(String cpf);

    /** Busca por parte do nome na tela do gestor, paginada. */
    Page<Cidadao> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
}
