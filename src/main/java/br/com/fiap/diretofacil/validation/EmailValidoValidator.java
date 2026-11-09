package br.com.fiap.diretofacil.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Locale;
import java.util.regex.Pattern;

/** Confere o formato do email e normaliza para minúsculas sem espaços, que é como fica gravado. */
public class EmailValidoValidator implements ConstraintValidator<EmailValido, String> {

    private static final Pattern FORMATO = Pattern.compile(
            "^[A-Za-z0-9][A-Za-z0-9._%+-]{0,63}@[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?(?:\\.[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?)*\\.[A-Za-z]{2,}$");

    @Override
    public boolean isValid(String valor, ConstraintValidatorContext context) {
        if (valor == null || valor.isBlank()) {
            return true;
        }
        String email = valor.trim();
        return email.length() <= 150
                && !email.contains("..")
                && FORMATO.matcher(email).matches();
    }

    /** Devolve o email em minúsculas e sem espaços nas pontas. */
    public static String normalizar(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}
