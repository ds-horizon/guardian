package com.dreamsportslabs.guardian.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class MfaFactorDto {
  @JsonProperty("factor")
  private String factor;

  @JsonProperty("is_enabled")
  private Boolean isEnabled;
}
