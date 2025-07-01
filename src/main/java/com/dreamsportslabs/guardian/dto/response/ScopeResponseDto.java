package com.dreamsportslabs.guardian.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ScopeResponseDto {
  private String name;
  private String displayName;
  private String description;
  private String iconUrl;
  private Boolean isOidc;
  private List<String> claims;
}
