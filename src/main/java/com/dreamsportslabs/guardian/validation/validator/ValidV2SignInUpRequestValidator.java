package com.dreamsportslabs.guardian.validation.validator;

import com.dreamsportslabs.guardian.dto.request.v2.V2SignInUpRequestDto;
import com.dreamsportslabs.guardian.validation.annotation.ValidV2SignInUpRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;

public class ValidV2SignInUpRequestValidator
    implements ConstraintValidator<ValidV2SignInUpRequest, V2SignInUpRequestDto> {

  @Override
  public boolean isValid(V2SignInUpRequestDto request, ConstraintValidatorContext context) {
    if (request == null) {
      return false;
    }

    long identifierCount =
        Stream.of(request.getUsername(), request.getEmail(), request.getPhoneNumber())
            .filter(StringUtils::isNotBlank)
            .count();

    if (identifierCount == 0) {
      context
          .buildConstraintViolationWithTemplate(
              "one of username, email or phone number is required")
          .addConstraintViolation();
      return false;
    }

    if (identifierCount > 1) {
      context
          .buildConstraintViolationWithTemplate(
              "only one of username, email or phone number is allowed")
          .addConstraintViolation();
      return false;
    }

    if (StringUtils.isAllBlank(request.getPassword(), request.getPin())) {
      context
          .buildConstraintViolationWithTemplate("one of password or pin is required")
          .addConstraintViolation();
      return false;
    }

    if (StringUtils.isNoneBlank(request.getPassword(), request.getPin())) {
      context
          .buildConstraintViolationWithTemplate("only one of password or pin is allowed")
          .addConstraintViolation();
      return false;
    }

    if (StringUtils.isBlank(request.getClientId())) {
      context
          .buildConstraintViolationWithTemplate("client_id is required")
          .addConstraintViolation();
      return false;
    }

    return true;
  }
}
