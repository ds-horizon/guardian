package com.dreamsportslabs.guardian.validation.validator;

import com.dreamsportslabs.guardian.constant.Constants;
import com.dreamsportslabs.guardian.dto.request.v2.V2AuthFbRequestDto;
import com.dreamsportslabs.guardian.validation.annotation.ValidV2AuthFbRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidV2AuthFbRequestValidator
    implements ConstraintValidator<ValidV2AuthFbRequest, V2AuthFbRequestDto> {

  private boolean isValidAccessToken(String accessToken, ConstraintValidatorContext context) {
    if (accessToken == null) {
      context
          .buildConstraintViolationWithTemplate("access_token cannot be null")
          .addConstraintViolation();
      return false;
    }
    return true;
  }

  private boolean isValidResponseType(String responseType, ConstraintValidatorContext context) {
    if (responseType == null) {
      context
          .buildConstraintViolationWithTemplate("response_type cannot be null")
          .addConstraintViolation();
      return false;
    }
    if (!Constants.fbAuthResponseTypes.contains(responseType)) {
      context
          .buildConstraintViolationWithTemplate("response_type is invalid")
          .addConstraintViolation();
      return false;
    }
    return true;
  }

  @Override
  public boolean isValid(V2AuthFbRequestDto request, ConstraintValidatorContext context) {
    if (request == null) {
      return false;
    }
    return isValidAccessToken(request.getAccessToken(), context)
        && isValidResponseType(request.getResponseType(), context);
  }
}
