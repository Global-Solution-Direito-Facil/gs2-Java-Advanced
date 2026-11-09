package br.com.fiap.diretofacil.repository;

import br.com.fiap.diretofacil.entity.Usuario;
import br.com.fiap.diretofacil.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** Acesso às contas de login. */
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /** Busca a conta no login; ignora maiúsculas porque o app pode mandar o email de qualquer jeito. */
    Optional<Usuario> findByEmailIgnoreCase(String email);

    /** Confere se o email já tem conta antes de registrar. */
    boolean existsByEmailIgnoreCase(String email);

    /** Confere se o CPF já tem conta antes de registrar. */
    boolean existsByCpf(String cpf);

    /** Lista contas por perfil, usada na tela de equipe do gestor. */
    Page<Usuario> findByPerfil(Role perfil, Pageable pageable);
}
