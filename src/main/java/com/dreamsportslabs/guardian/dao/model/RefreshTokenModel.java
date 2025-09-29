package com.dreamsportslabs.guardian.dao.model;

import com.dreamsportslabs.guardian.constant.AuthMethod;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class RefreshTokenModel {
  private String tenantId;
  private String clientId;
  private String userId;
  private Boolean isActive;
  private String refreshToken;
  private long refreshTokenExp;
  private List<String> scope;
  private String deviceName;
  private String ip;
  private String location;
  private String source;
  @Builder.Default private List<AuthMethod> authMethod = new ArrayList<>();
}
