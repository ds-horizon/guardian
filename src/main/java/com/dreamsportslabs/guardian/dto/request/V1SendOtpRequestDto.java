package com.dreamsportslabs.guardian.dto.request;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_REQUEST;

import com.dreamsportslabs.guardian.constant.Contact;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@Slf4j
public class V1SendOtpRequestDto {
  private String state;
  private Contact contact;

  private boolean isValidState() {
    return state != null;
  }

  private boolean isValidContact() {
    if (contact == null) {
      return false;
    }
    return contact.validate();
  }

  public void validate() {
    if (isValidState()) {
      return;
    }

    if (!isValidContact()) {
      throw INVALID_REQUEST.getCustomException("contact details are missing or invalid");
    }
  }
}
