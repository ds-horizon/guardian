package com.dreamsportslabs.guardian.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CodeResponseDto {
  private String code;
  private Integer expiresIn;
}
