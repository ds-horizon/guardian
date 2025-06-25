package com.dreamsportslabs.guardian.dto.request;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_REQUEST;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@Slf4j
public class VerifyOtpRequestDto {
  private String state;
  private String otp;

  public void validate() {
    if (state == null || state.isEmpty()) {
      throw INVALID_REQUEST.getCustomException("State is missing");
    }
    if (otp == null || otp.isEmpty()) {
      throw INVALID_REQUEST.getCustomException("OTP is missing");
    }
  }
}
