package br.com.fiap.diretofacil.repository;

import br.com.fiap.diretofacil.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /** Busca a conta no login; ignora maiúsculas porque o app pode mandar o email de qualquer jeito. */
    Optional<Usuario> findByEmailIgnoreCase(String email);

    /** Confere se o email já tem conta antes de registrar um cidadão ou criar um gestor. */
    boolean existsByEmailIgnoreCase(String email);

    /** Encontra a conta de um cidadão, para o mapper devolver o email junto com o perfil. */
    Optional<Usuario> findByCidadaoId(Long cidadaoId);

    /** Encontra a conta de um gestor, para o mapper devolver o email junto com o perfil. */
    Optional<Usuario> findByGestorId(Long gestorId);
}
