package com.dreamsportslabs.guardian.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class V1ContactBlockedFlowsResponseDto {
  private String contact;
  private List<String> blockedFlows;
  private int totalCount;
}
