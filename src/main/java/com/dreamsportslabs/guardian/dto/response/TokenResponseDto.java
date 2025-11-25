package com.dreamsportslabs.guardian.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TokenResponseDto {
  private String accessToken;
  private String refreshToken;
  private String idToken;
  private String ssoToken;
  private String tokenType;
  private Integer expiresIn;
  private Boolean isNewUser;

  @JsonProperty("mfa_factors")
  private List<MfaFactorDto> mfaFactors;
}
