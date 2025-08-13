package com.dreamsportslabs.guardian.dto.request.scope;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_REQUEST;
import static com.dreamsportslabs.guardian.utils.ScopeUtil.validateClaims;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class CreateScopeRequestDto {

  @JsonProperty("name")
  private String name;

  @JsonProperty("display_name")
  private String displayName;

  @JsonProperty("description")
  private String description;

  @JsonProperty("claims")
  private List<String> claims = new ArrayList<>();

  @JsonProperty("icon_url")
  private String iconUrl;

  @JsonProperty("is_oidc")
  private Boolean isOidc = Boolean.FALSE;

  public void validate() {
    if (StringUtils.isBlank(name)) {
      throw INVALID_REQUEST.getCustomException("scope name is required");
    }

    if (displayName != null && StringUtils.isBlank(displayName)) {
      throw INVALID_REQUEST.getCustomException("Display name cannot be empty");
    }

    validateClaims(name, claims);
  }
}
