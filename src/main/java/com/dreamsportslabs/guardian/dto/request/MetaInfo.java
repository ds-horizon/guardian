package com.dreamsportslabs.guardian.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MetaInfo {
  @JsonProperty("location")
  private String location;

  @JsonProperty("device_name")
  private String deviceName;

  @JsonProperty("ip")
  private String ip;

  @JsonProperty("source")
  private String source;

  // TODO: remove once v1 is deprecated
  @JsonProperty("deviceName")
  private String legacyDeviceName;

  // TODO: remove once v1 is deprecated

  public String getDeviceName() {
    if (deviceName != null) {
      return deviceName;
    }
    return legacyDeviceName;
  }
}
