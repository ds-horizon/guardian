package com.dreamsportslabs.guardian.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ScopeListResponseDto {
  private List<ScopeResponseDto> scopes;
}
