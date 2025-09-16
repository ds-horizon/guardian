package com.dreamsportslabs.guardian.dao.model;

import com.dreamsportslabs.guardian.constant.AuthMethod;
import com.dreamsportslabs.guardian.dto.request.MetaInfo;
import java.util.List;
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
  private String clientId;
  private List<String> scopes;
  private List<AuthMethod> authMethods;
  private MetaInfo metaInfo;
  private Integer expiry;
}
