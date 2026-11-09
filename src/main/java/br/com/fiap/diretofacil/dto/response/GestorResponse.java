package br.com.fiap.diretofacil.dto.response;

import java.time.LocalDateTime;

/** Perfil completo do gestor com o email da conta. */
public record GestorResponse(
        Long id,
        String nome,
        String cpf,
        String matricula,
        String unidade,
        String telefone,
        Boolean ativo,
        String email,
        LocalDateTime dataCadastro
) {}
