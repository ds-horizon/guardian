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
public class V1UnblockContactFlowRequestDto {
  private String contact;
  private List<String> unblockFlows;
  private String operator;
  @JsonIgnore private Map<String, Object> additionalInfo;

  public V1UnblockContactFlowRequestDto() {
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

    if (StringUtils.isBlank(operator)) {
      throw INVALID_REQUEST.getCustomException("Operator is required");
    }

    if (unblockFlows == null || unblockFlows.isEmpty()) {
      throw INVALID_REQUEST.getCustomException("At least one flow must be provided");
    }

    // Validate that all flows are valid
    for (String flow : unblockFlows) {
      try {
        BlockFlow.fromString(flow);
      } catch (IllegalArgumentException e) {
        throw INVALID_REQUEST.getCustomException(
            "Invalid flow: " + flow + ". Valid flows are: " + BlockFlow.getAllFlowNames());
      }
    }
  }
}
