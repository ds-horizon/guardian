package com.dreamsportslabs.guardian.dao.model;

import com.dreamsportslabs.guardian.constant.AuthMethod;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class SsoTokenModel {
  private String tenantId;
  private String userId;
  private String clientIdIssuedTo;
  private Boolean isActive;
  private Long expiry;
  private String refreshToken;
  private String ssoToken;
  private List<String> clientIdUsedBy;
  private List<AuthMethod> authMethods;
}
