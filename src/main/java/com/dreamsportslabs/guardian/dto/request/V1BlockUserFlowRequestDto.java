package com.dreamsportslabs.guardian.dto.request;

import static com.dreamsportslabs.guardian.constant.Constants.MILLIS_TO_SECONDS;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_REQUEST;

import com.dreamsportslabs.guardian.constant.BlockFlow;
import java.util.List;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class V1BlockUserFlowRequestDto {
  private String userIdentifier;
  private List<String> blockFlows;
  private String reason;
  private Long unblockedAt;

  public void validate() {
    if (StringUtils.isBlank(userIdentifier)) {
      throw INVALID_REQUEST.getCustomException("userIdentifier is required");
    }

    if (blockFlows == null || blockFlows.isEmpty()) {
      throw INVALID_REQUEST.getCustomException("At least one flow must be provided");
    }

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

    if (unblockedAt == null) {
      throw INVALID_REQUEST.getCustomException("unblockedAt is required");
    } else {
      Long currentTimestamp = System.currentTimeMillis() / MILLIS_TO_SECONDS;
      if (unblockedAt <= currentTimestamp) {
        throw INVALID_REQUEST.getCustomException("unblockedAt must be a future timestamp");
      }
    }
  }
}
