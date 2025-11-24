package com.dreamsportslabs.guardian.validation.annotation;

import jakarta.validation.Payload;

public @interface ValidV2MfaSignInRequest {
  String message() default "Invalid Request";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
