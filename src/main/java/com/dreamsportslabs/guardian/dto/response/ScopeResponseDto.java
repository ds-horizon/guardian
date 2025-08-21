package com.dreamsportslabs.guardian.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ScopeResponseDto {
  private String name;

  @JsonProperty("display_name")
  private String displayName;

  private String description;

  @JsonProperty("icon_url")
  private String iconUrl;

  @JsonProperty("is_oidc")
  private Boolean isOidc;

  private List<String> claims;
}
