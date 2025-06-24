package com.dreamsportslabs.guardian.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScopeResponseDto {
  private String scope;
  private String displayName;
  private String description;
  private String iconUrl;
  private Boolean isOidc;
  private List<String> claims;
}
