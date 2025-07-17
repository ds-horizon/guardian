package com.dreamsportslabs.guardian.dto.request;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_REQUEST;

import com.dreamsportslabs.guardian.constant.BlockFlow;
import java.util.List;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class V1UnblockUserFlowRequestDto {
  private String userIdentifier;
  private List<String> unblockFlows;

  public void validate() {
    if (StringUtils.isBlank(userIdentifier)) {
      throw INVALID_REQUEST.getCustomException("userIdentifier is required");
    }

    if (unblockFlows == null || unblockFlows.isEmpty()) {
      throw INVALID_REQUEST.getCustomException("At least one flow must be provided");
    }

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
