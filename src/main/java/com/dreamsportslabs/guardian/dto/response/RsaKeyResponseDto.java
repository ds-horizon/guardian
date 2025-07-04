package com.dreamsportslabs.guardian.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
@Getter
public class RsaKeyResponseDto {
  private String kid;
  private Object publicKey;
  private Object privateKey;
  private Integer keySize;
}
