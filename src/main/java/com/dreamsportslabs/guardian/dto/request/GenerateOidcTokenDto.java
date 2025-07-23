package com.dreamsportslabs.guardian.dto.request;

import static com.dreamsportslabs.guardian.constant.Constants.SECONDS_TO_MILLISECONDS;

import io.vertx.core.json.JsonObject;
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
  private List<String> scope;
  private JsonObject userResponse;
  @Builder.Default private long iat = System.currentTimeMillis() / SECONDS_TO_MILLISECONDS;
  private String deviceName;
  private String ip;
}
