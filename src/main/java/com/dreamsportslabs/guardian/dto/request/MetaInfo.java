package com.dreamsportslabs.guardian.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MetaInfo {
  private String location;
  private String deviceName;
  private String ip;
  private String source;
}
