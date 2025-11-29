package com.dreamsportslabs.guardian.validation.validator;

import com.dreamsportslabs.guardian.constant.MfaFactor;
import com.dreamsportslabs.guardian.dto.request.v2.V2MfaSignInRequestDto;
import com.dreamsportslabs.guardian.validation.annotation.ValidV2MfaSignInRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;

public class ValidV2MfaSignInRequestValidator
    implements ConstraintValidator<ValidV2MfaSignInRequest, V2MfaSignInRequestDto> {

  @Override
  public boolean isValid(V2MfaSignInRequestDto request, ConstraintValidatorContext context) {
    if (request == null) {
      return false;
    }

    if (request.getFactor() == null) {
      context.buildConstraintViolationWithTemplate("factor is required").addConstraintViolation();
      return false;
    }

    MfaFactor factor = request.getFactor();

    // Validate identifier (required for credential-based factors)
    if (requiresIdentifier(factor)) {
      long identifierCount =
          Stream.of(request.getUsername(), request.getEmail(), request.getPhoneNumber())
              .filter(StringUtils::isNotBlank)
              .count();

      if (identifierCount != 1) {
        context
            .buildConstraintViolationWithTemplate(
                "exactly one of username, email or phone_number is required")
            .addConstraintViolation();
        return false;
      }
    }

    // Validate credential based on factor
    if (factor == MfaFactor.PASSWORD) {
      if (StringUtils.isBlank(request.getPassword())) {
        context
            .buildConstraintViolationWithTemplate("password is required for password factor")
            .addConstraintViolation();
        return false;
      }
    } else if (factor == MfaFactor.PIN) {
      if (StringUtils.isBlank(request.getPin())) {
        context
            .buildConstraintViolationWithTemplate("pin is required for pin factor")
            .addConstraintViolation();
        return false;
      }
    }

    if (StringUtils.isBlank(request.getRefreshToken())) {
      context
          .buildConstraintViolationWithTemplate("refresh_token is required")
          .addConstraintViolation();
      return false;
    }

    return true;
  }

  private boolean requiresIdentifier(MfaFactor factor) {
    // PASSWORD and PIN factors require identifier
    return factor == MfaFactor.PASSWORD || factor == MfaFactor.PIN;
  }
}
