package com.dreamsportslabs.guardian.validation.validator;

import com.dreamsportslabs.guardian.constant.Channel;
import com.dreamsportslabs.guardian.constant.Constants;
import com.dreamsportslabs.guardian.constant.Contact;
import com.dreamsportslabs.guardian.dto.request.v2.V2PasswordlessInitRequestDto;
import com.dreamsportslabs.guardian.validation.annotation.ValidV2PasswordlessInitRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ValidV2PasswordlessInitRequestValidator
    implements ConstraintValidator<ValidV2PasswordlessInitRequest, V2PasswordlessInitRequestDto> {

  private boolean isValidContact(List<Contact> contacts, ConstraintValidatorContext context) {
    if (contacts == null || contacts.isEmpty()) {
      context
          .buildConstraintViolationWithTemplate("contacts list is empty")
          .addConstraintViolation();
      return false;
    }
    Set<Channel> uniqueChannels = new HashSet<>();
    for (Contact contact : contacts) {
      if (!contact.validate()) {
        context.buildConstraintViolationWithTemplate("contact is invalid").addConstraintViolation();
        return false;
      }
      if (!uniqueChannels.add(contact.getChannel())) {
        return false;
      }
    }
    return true;
  }

  private boolean isValidResponseType(String responseType, ConstraintValidatorContext context) {
    if (Constants.passwordlessAuthResponseTypes.contains(responseType)) {
      return true;
    }
    context
        .buildConstraintViolationWithTemplate("response_type is invalid")
        .addConstraintViolation();
    return false;
  }

  @Override
  public boolean isValid(V2PasswordlessInitRequestDto request, ConstraintValidatorContext context) {
    if (request == null) {
      return false;
    }
    if (request.getState() != null) {
      return true;
    }
    return isValidContact(request.getContacts(), context)
        && isValidResponseType(request.getResponseType(), context);
  }
}
