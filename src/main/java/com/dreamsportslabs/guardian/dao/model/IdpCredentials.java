package com.dreamsportslabs.guardian.dao.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class IdpCredentials {
  private String accessToken;
  private String refreshToken;
  private String idToken;
}
