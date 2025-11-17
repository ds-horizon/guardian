package com.dreamsportslabs.guardian.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeviceMetadata {
  @JsonProperty("platform")
  private String platform;

  @JsonProperty("device_model")
  private String deviceModel;

  @JsonProperty("os_version")
  private String osVersion;

  @JsonProperty("app_version")
  private String appVersion;

  @JsonProperty("device_name")
  private String deviceName;
}
