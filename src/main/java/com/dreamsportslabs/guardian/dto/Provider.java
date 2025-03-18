package com.dreamsportslabs.guardian.dto;

import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class Provider {
  private String name;
  private String providerUserId;
  private Map<String, Object> data;
  private Map<String, Object> credentials;
}
