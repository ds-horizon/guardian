package com.dreamsportslabs.guardian.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public class ClientListResponseDto {
  @JsonProperty("clients")
  private List<ClientResponseDto> clients;

  @JsonProperty("page")
  private Integer page;

  @JsonProperty("pageSize")
  private Integer pageSize;
}
