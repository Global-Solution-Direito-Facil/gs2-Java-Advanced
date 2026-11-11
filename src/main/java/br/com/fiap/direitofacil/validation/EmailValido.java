package br.com.fiap.direitofacil.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/** Valida email exigindo usuário, arroba e domínio com ponto; mais rigoroso que o @Email padrão. */
@Documented
@Constraint(validatedBy = EmailValidoValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface EmailValido {

    String message() default "Email inválido";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
