package com.company.passportnumber.entity;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Checks that Customer has either phone or email
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidPassportNumberValidator.class)
public @interface ValidPassportNumber {
    String message() default "Passport number is not valid";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
