package com.dreamsportslabs.guardian.dto.request;

import static com.dreamsportslabs.guardian.utils.Utils.getCurrentTimeInSeconds;

import com.dreamsportslabs.guardian.constant.AuthMethod;
import io.vertx.core.json.JsonObject;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Getter
@Builder
public class GenerateOidcTokenDto {
  private String userId;
  private String clientId;
  private String tenantId;
  private String nonce;
  private String scope;
  private JsonObject userResponse;
  @Builder.Default private long iat = getCurrentTimeInSeconds();
  private String deviceName;
  @Builder.Default private List<AuthMethod> authMethods = new ArrayList<>();
  private String ip;
}
