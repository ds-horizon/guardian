package com.dreamsportslabs.guardian.dto.request;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_REQUEST;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class V1BlockContactRequestDto {
  private String contact;
  private List<String> blockApis;
  private String reason;
  private Long unblockedAt;
  private String operator;
  @JsonIgnore private Map<String, Object> additionalInfo;

  public V1BlockContactRequestDto() {
    this.additionalInfo = new HashMap<>();
  }

  @JsonAnyGetter
  public Map<String, Object> getAdditionalInfo() {
    return this.additionalInfo;
  }

  @JsonAnySetter
  public void addAdditionalInfo(String key, Object value) {
    this.additionalInfo.put(key, value);
  }

  public void validate() {
    if (contact == null || contact.trim().isEmpty()) {
      throw INVALID_REQUEST.getCustomException("Contact is required");
    }

    if (blockApis == null || blockApis.isEmpty()) {
      throw INVALID_REQUEST.getCustomException("At least one API must be provided");
    }

    if (reason == null || reason.trim().isEmpty()) {
      throw INVALID_REQUEST.getCustomException("Reason is required");
    }

    if (StringUtils.isBlank(operator)) {
      throw INVALID_REQUEST.getCustomException("Operator is required");
    }

    if (unblockedAt != null) {
      Long currentTimestamp = System.currentTimeMillis() / 1000;
      if (unblockedAt <= currentTimestamp) {
        throw INVALID_REQUEST.getCustomException("unblockedAt must be a future timestamp");
      }
    }
  }
}
