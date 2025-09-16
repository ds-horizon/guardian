package com.dreamsportslabs.guardian.validation.annotation;

import com.dreamsportslabs.guardian.validation.validator.ValidV2PasswordlessInitRequestValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidV2PasswordlessInitRequestValidator.class)
public @interface ValidV2PasswordlessInitRequest {
  String message() default "Invalid Passwordless Init Request";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
