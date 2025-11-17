package com.dreamsportslabs.guardian.dto.response.v2;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class V2WebAuthnStartResponseDto {
  @JsonProperty("recommended_mode")
  private String recommendedMode; // "assert" or "enroll"

  @JsonProperty("assert")
  private AssertBlock assertBlock;

  @JsonProperty("enroll")
  private EnrollBlock enrollBlock;

  @Getter
  @Builder
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class AssertBlock {
    @JsonProperty("state")
    private String state;

    @JsonProperty("options")
    private Object options; // PublicKeyCredentialRequestOptions - will be a Map/JsonObject
  }

  @Getter
  @Builder
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class EnrollBlock {
    @JsonProperty("state")
    private String state;

    @JsonProperty("options")
    private Object options; // PublicKeyCredentialCreationOptions - will be a Map/JsonObject
  }
}
