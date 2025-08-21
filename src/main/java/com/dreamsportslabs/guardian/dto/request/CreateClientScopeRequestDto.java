package com.dreamsportslabs.guardian.dto.request;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_REQUEST;

import java.util.List;
import lombok.Data;

@Data
public class CreateClientScopeRequestDto {
  private List<String> scopes;

  public void validate() {
    if (this.scopes == null || this.scopes.isEmpty()) {
      throw INVALID_REQUEST.getCustomException("Scope is required");
    }
  }
}
