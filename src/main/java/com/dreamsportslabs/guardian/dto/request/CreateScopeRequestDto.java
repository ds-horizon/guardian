package com.dreamsportslabs.guardian.dto.request;

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
  private String scope;
  private String displayName;
  private String description;
  private List<String> claims;

  public void validate() {
    if (scope == null || scope.isBlank()) {
      throw INVALID_REQUEST.getCustomException("scope is required");
    }
    if (displayName == null || displayName.isBlank()) {
      throw INVALID_REQUEST.getCustomException("display_name is required");
    }
    if (description == null || description.isBlank()) {
      throw INVALID_REQUEST.getCustomException("description is required");
    }
    if (claims == null || claims.isEmpty()) {
      throw INVALID_REQUEST.getCustomException("claims is required");
    }
  }
}
