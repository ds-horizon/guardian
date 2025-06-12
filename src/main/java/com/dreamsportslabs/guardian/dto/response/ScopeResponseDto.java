package com.dreamsportslabs.guardian.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScopeResponseDto {
    private Integer id;
    private String scope;
    private String displayName;
    private String description;
    private List<String> claims;
}
