package com.dreamsportslabs.guardian.dto.response;

import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@NoArgsConstructor
@Setter
public class ClientScopeResponseDto {
  private List<String> scopes;
}
