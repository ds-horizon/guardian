package com.dreamsportslabs.guardian.dto.request.v1;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_REQUEST;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@NoArgsConstructor
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@Slf4j
public class V1PasswordlessCompleteRequestDto {
  @NotBlank(message = "Invalid State")
  private String state;

  @NotBlank(message = "Invalid OTP")
  private String otp;

  public void validate() {
    if (state == null) {
      throw INVALID_REQUEST.getCustomException("Invalid state");
    }
  }
}
