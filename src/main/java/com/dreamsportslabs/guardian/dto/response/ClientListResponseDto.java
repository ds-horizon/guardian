package com.dreamsportslabs.guardian.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClientListResponseDto {
  private List<ClientResponseDto> clients;
  private Integer page;
  private Integer limit;
}
