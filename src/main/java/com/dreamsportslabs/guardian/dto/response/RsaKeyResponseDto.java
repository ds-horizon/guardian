package com.dreamsportslabs.guardian.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public class RsaKeyResponseDto {

  @JsonProperty("kid")
  private String kid;

  @JsonProperty("publicKey")
  private Object publicKey;

  @JsonProperty("privateKey")
  private Object privateKey;

  @JsonProperty("keySize")
  private Integer keySize;
}
