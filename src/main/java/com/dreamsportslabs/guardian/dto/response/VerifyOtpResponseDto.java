package com.dreamsportslabs.guardian.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Builder
@Slf4j
public class VerifyOtpResponseDto {
  private Boolean success;
}
