package com.dreamsportslabs.guardian.dto.request;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_REQUEST;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import net.logstash.logback.util.StringUtils;

@Data
public class V1UnblockContactRequestDto {
  private String contact;
  private List<String> unblockApis;
  private String operator;
  @JsonIgnore private Map<String, Object> additionalInfo;

  public V1UnblockContactRequestDto() {
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

    if (StringUtils.isBlank(operator)) {
      throw INVALID_REQUEST.getCustomException("Operator is required");
    }

    if (unblockApis == null || unblockApis.isEmpty()) {
      throw INVALID_REQUEST.getCustomException("At least one API must be provided");
    }
  }
}
