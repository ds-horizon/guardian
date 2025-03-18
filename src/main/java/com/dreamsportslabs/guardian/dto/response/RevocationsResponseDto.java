package com.dreamsportslabs.guardian.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class RevocationsResponseDto {
  private List<String> revocations;
  private Long from;
  private Long to;
  private Integer expiry;
}
