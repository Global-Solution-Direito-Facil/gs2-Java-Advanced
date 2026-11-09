package br.com.fiap.diretofacil.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/** Confere formato e dígitos verificadores do CPF e normaliza para a máscara 000.000.000-00 do banco. */
public class CpfValidator implements ConstraintValidator<Cpf, String> {

    @Override
    public boolean isValid(String valor, ConstraintValidatorContext context) {
        if (valor == null || valor.isBlank()) {
            return true;
        }
        String digitos = somenteDigitos(valor);
        if (digitos.length() != 11 || digitos.chars().distinct().count() == 1) {
            return false;
        }
        return digitoVerificador(digitos, 9) == digitos.charAt(9) - '0'
                && digitoVerificador(digitos, 10) == digitos.charAt(10) - '0';
    }

    /** Devolve o CPF no formato 000.000.000-00, ou o valor original se não tiver 11 dígitos. */
    public static String normalizar(String cpf) {
        if (cpf == null) {
            return null;
        }
        String digitos = somenteDigitos(cpf);
        if (digitos.length() != 11) {
            return cpf.trim();
        }
        return digitos.substring(0, 3) + "." + digitos.substring(3, 6) + "."
                + digitos.substring(6, 9) + "-" + digitos.substring(9);
    }

    private static String somenteDigitos(String valor) {
        return valor.replaceAll("\\D", "");
    }

    private static int digitoVerificador(String digitos, int tamanho) {
        int soma = 0;
        for (int i = 0; i < tamanho; i++) {
            soma += (digitos.charAt(i) - '0') * (tamanho + 1 - i);
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }
}
