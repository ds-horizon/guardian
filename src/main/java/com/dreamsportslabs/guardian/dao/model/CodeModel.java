package com.dreamsportslabs.guardian.dao.model;

import com.dreamsportslabs.guardian.dto.request.MetaInfo;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Builder
@Jacksonized
public class CodeModel {
  private String code;
  private Map<String, Object> user;
  private MetaInfo metaInfo;
  private Integer expiry;
}
