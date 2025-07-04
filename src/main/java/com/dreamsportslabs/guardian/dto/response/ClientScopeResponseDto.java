package com.dreamsportslabs.guardian.dto.response;

import java.util.List;
import lombok.Data;

@Data
public class ClientScopeResponseDto {
  private List<String> scopes;
}
