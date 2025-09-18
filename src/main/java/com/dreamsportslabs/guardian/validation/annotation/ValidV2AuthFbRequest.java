package com.dreamsportslabs.guardian.validation.annotation;

import com.dreamsportslabs.guardian.validation.validator.ValidV2AuthFbRequestValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidV2AuthFbRequestValidator.class)
public @interface ValidV2AuthFbRequest {
  String message() default "Invalid Facebook Auth Request";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
