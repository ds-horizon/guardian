package com.dreamsportslabs.guardian.dto.request.v2;

import com.dreamsportslabs.guardian.dto.request.DeviceMetadata;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
public class V2WebAuthnFinishRequestDto {
  @JsonProperty("client_id")
  @NotNull(message = "client_id cannot be null or empty")
  private String clientId;

  @JsonProperty("state")
  @NotNull(message = "state cannot be null or empty")
  private String state;

  @JsonProperty("credential")
  @NotNull(message = "credential cannot be null")
  private CredentialDto credential;

  @JsonProperty("device_metadata")
  private DeviceMetadata deviceMetadata;

  @Getter
  @Setter
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @NoArgsConstructor
  public static class CredentialDto {
    @JsonProperty("id")
    @NotNull(message = "credential.id cannot be null")
    private String id;

    @JsonProperty("type")
    @NotNull(message = "credential.type cannot be null")
    private String type;

    @JsonProperty("response")
    @NotNull(message = "credential.response cannot be null")
    private ResponseDto response;
  }

  @Getter
  @Setter
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @NoArgsConstructor
  public static class ResponseDto {
    // For enrollment (attestation)
    @JsonProperty("attestationObject")
    private String attestationObject;

    @JsonProperty("clientDataJSON")
    private String clientDataJSON;

    // For assertion (login)
    @JsonProperty("authenticatorData")
    private String authenticatorData;

    @JsonProperty("signature")
    private String signature;

    @JsonProperty("userHandle")
    private String userHandle;
  }
}
