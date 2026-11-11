package br.com.fiap.direitofacil.dto.request;

import br.com.fiap.direitofacil.validation.Cpf;
import br.com.fiap.direitofacil.validation.EmailValido;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Criação de conta: o cidadão em POST /auth/registrar e o gestor em POST /gestores. */
public record UsuarioRequest(
        @NotBlank @Size(max = 150) String nome,
        @NotBlank @Cpf String cpf,
        @NotBlank @EmailValido @Size(max = 150) String email,
        @NotBlank @Size(min = 8, max = 72) String senha
) {}
