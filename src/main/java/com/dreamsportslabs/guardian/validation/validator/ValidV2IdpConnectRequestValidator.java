package com.dreamsportslabs.guardian.validation.validator;

import com.dreamsportslabs.guardian.dto.request.v2.V2IdpConnectRequestDto;
import com.dreamsportslabs.guardian.validation.annotation.ValidV2IdpConnectRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

public class ValidV2IdpConnectRequestValidator
    implements ConstraintValidator<ValidV2IdpConnectRequest, V2IdpConnectRequestDto> {

  private boolean isValidIdProvider(String idProvider, ConstraintValidatorContext context) {
    if (StringUtils.isBlank(idProvider)) {
      context
          .buildConstraintViolationWithTemplate("id_provider is required")
          .addConstraintViolation();
      return false;
    }
    return true;
  }

  private boolean isValidIdentifier(String identifier, ConstraintValidatorContext context) {
    if (StringUtils.isBlank(identifier)) {
      context
          .buildConstraintViolationWithTemplate("identifier is required")
          .addConstraintViolation();
      return false;
    }
    return true;
  }

  private boolean isValidResponseType(String responseType, ConstraintValidatorContext context) {
    if (StringUtils.isBlank(responseType)) {
      context
          .buildConstraintViolationWithTemplate("response_type is required")
          .addConstraintViolation();
      return false;
    }
    return true;
  }

  @Override
  public boolean isValid(V2IdpConnectRequestDto request, ConstraintValidatorContext context) {
    if (request == null) {
      return false;
    }
    return isValidIdProvider(request.getIdProvider(), context)
        && isValidIdentifier(request.getIdentifier(), context)
        && isValidResponseType(request.getResponseType(), context);
  }
}
