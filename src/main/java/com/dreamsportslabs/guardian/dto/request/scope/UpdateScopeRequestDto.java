package com.dreamsportslabs.guardian.dto.request.scope;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_REQUEST;
import static com.dreamsportslabs.guardian.utils.ScopeUtil.validateClaims;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class UpdateScopeRequestDto {

  @JsonProperty("display_name")
  private String displayName;

  private String description;

  private List<String> claims;

  @JsonProperty("icon_url")
  private String iconUrl;

  @JsonProperty("is_oidc")
  private Boolean isOidc;

  public void validate(String scopeName) {
    if (displayName != null && StringUtils.isBlank(displayName)) {
      throw INVALID_REQUEST.getCustomException("Display name cannot be empty");
    }

    if (claims != null && !claims.isEmpty()) {
      validateClaims(scopeName, claims);
    }
  }
}
