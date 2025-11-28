package com.dreamsportslabs.guardian.dto.request.v2;

import com.dreamsportslabs.guardian.dto.request.DeviceMetadata;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
  @Size(max = 32, message = "client_id must not exceed 32 characters")
  private String clientId;

  @JsonProperty("state")
  @NotNull(message = "state cannot be null or empty")
  @Size(max = 256, message = "state must not exceed 256 characters")
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
    @Size(max = 1024, message = "credential.id must not exceed 1024 characters")
    private String id;

    @JsonProperty("type")
    @NotNull(message = "credential.type cannot be null")
    @Size(max = 50, message = "credential.type must not exceed 50 characters")
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
    @Size(max = 30000, message = "attestationObject must not exceed 30000 characters")
    private String attestationObject;

    @JsonProperty("clientDataJSON")
    @Size(max = 12000, message = "clientDataJSON must not exceed 12000 characters")
    private String clientDataJSON;

    // For assertion (login)
    @JsonProperty("authenticatorData")
    @Size(max = 1200, message = "authenticatorData must not exceed 1200 characters")
    private String authenticatorData;

    @JsonProperty("signature")
    @Size(max = 1200, message = "signature must not exceed 1200 characters")
    private String signature;

    @JsonProperty("userHandle")
    @Size(max = 1024, message = "userHandle must not exceed 1024 characters")
    private String userHandle;
  }
}
