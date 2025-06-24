package com.dreamsportslabs.guardian.dto.request;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_REQUEST;

import com.dreamsportslabs.guardian.constant.BlockFlow;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class V1BlockContactFlowRequestDto {
  private String contact;
  private List<String> blockFlows;
  private String reason;
  private Long unblockedAt;
  private String operator;
  @JsonIgnore private Map<String, Object> additionalInfo;

  public V1BlockContactFlowRequestDto() {
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
    if (StringUtils.isBlank(contact)) {
      throw INVALID_REQUEST.getCustomException("Contact is required");
    }

    if (blockFlows == null || blockFlows.isEmpty()) {
      throw INVALID_REQUEST.getCustomException("At least one flow must be provided");
    }

    // Validate that all flows are valid
    for (String flow : blockFlows) {
      try {
        BlockFlow.fromString(flow);
      } catch (IllegalArgumentException e) {
        throw INVALID_REQUEST.getCustomException(
            "Invalid flow: " + flow + ". Valid flows are: " + BlockFlow.getAllFlowNames());
      }
    }

    if (StringUtils.isBlank(reason)) {
      throw INVALID_REQUEST.getCustomException("Reason is required");
    }

    if (StringUtils.isBlank(operator)) {
      throw INVALID_REQUEST.getCustomException("Operator is required");
    }

    if (unblockedAt == null) {
      throw INVALID_REQUEST.getCustomException("unblockedAt is required");
    } else {
      Long currentTimestamp = System.currentTimeMillis() / 1000;
      if (unblockedAt <= currentTimestamp) {
        throw INVALID_REQUEST.getCustomException("unblockedAt must be a future timestamp");
      }
    }
  }
}
