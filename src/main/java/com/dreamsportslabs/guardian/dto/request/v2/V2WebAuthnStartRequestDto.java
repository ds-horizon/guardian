package com.dreamsportslabs.guardian.dto.request.v2;

import com.dreamsportslabs.guardian.dto.request.DeviceMetadata;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@Slf4j
@NoArgsConstructor
public class V2WebAuthnStartRequestDto {
  @JsonProperty("client_id")
  @NotNull(message = "client_id cannot be null or empty")
  private String clientId;

  @JsonProperty("device_metadata")
  private DeviceMetadata deviceMetadata;
}
