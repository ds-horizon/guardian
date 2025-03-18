package com.dreamsportslabs.guardian.dto.request;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_REQUEST;

import lombok.Data;

@Data
public class V1CodeTokenExchangeRequestDto {
  private String code;

  public void validate() {
    if (this.code == null) {
      throw INVALID_REQUEST.getCustomException("Invalid code");
    }
  }
}
