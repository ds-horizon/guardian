package com.dreamsportslabs.guardian.validation.validator;

import com.dreamsportslabs.guardian.constant.Constants;
import com.dreamsportslabs.guardian.dto.request.v2.V2AuthGoogleRequestDto;
import com.dreamsportslabs.guardian.validation.annotation.ValidV2AuthGoogleRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

public class ValidV2AuthGoogleRequestValidator
    implements ConstraintValidator<ValidV2AuthGoogleRequest, V2AuthGoogleRequestDto> {

  private boolean isValidIdToken(String idToken, ConstraintValidatorContext context) {
    if (StringUtils.isEmpty(idToken)) {
      context
          .buildConstraintViolationWithTemplate("id_token cannot be null or empty")
          .addConstraintViolation();
      return false;
    }
    return true;
  }

  private boolean isValidResponseType(String responseType, ConstraintValidatorContext context) {
    if (StringUtils.isEmpty(responseType)) {
      context
          .buildConstraintViolationWithTemplate("response_type cannot be null or empty")
          .addConstraintViolation();
      return false;
    }
    if (!Constants.googleAuthResponseTypes.contains(responseType)) {
      context
          .buildConstraintViolationWithTemplate("response_type is invalid")
          .addConstraintViolation();
      return false;
    }
    return true;
  }

  @Override
  public boolean isValid(V2AuthGoogleRequestDto request, ConstraintValidatorContext context) {
    if (request == null) {
      return false;
    }
    return isValidIdToken(request.getIdToken(), context)
        && isValidResponseType(request.getResponseType(), context);
  }
}
