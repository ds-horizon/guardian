package com.dreamsportslabs.guardian.dto.request.scope;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_REQUEST;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateScopeRequestDto {
  private String name;
  private String displayName;
  private String description;
  private List<String> claims;
  private String iconUrl;
  private Boolean isOidc;

  public void validate() {
    if (name == null || name.isBlank()) {
      throw INVALID_REQUEST.getCustomException("scope name is required");
    }

    if (isOidc == null) {
      throw INVALID_REQUEST.getCustomException("isOidc field is required");
    }
  }
}
