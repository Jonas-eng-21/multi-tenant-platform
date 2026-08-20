package br.com.jonas.multitenant.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;


@Documented
@Constraint(validatedBy = CpfConstraintValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface CPF {

    String message() default "CPF inválido";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
