package br.com.fiap.diretofacil.repository;

import br.com.fiap.diretofacil.entity.Gestor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GestorRepository extends JpaRepository<Gestor, Long> {

    /** Impede CPF duplicado ao criar um gestor. */
    boolean existsByCpf(String cpf);

    /** Impede matrícula duplicada ao criar um gestor. */
    boolean existsByMatricula(String matricula);

    /** Lista só os gestores ativos na tela de equipe. */
    Page<Gestor> findByAtivoTrue(Pageable pageable);
}
