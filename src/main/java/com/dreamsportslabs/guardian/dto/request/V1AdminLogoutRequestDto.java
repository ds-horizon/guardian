package com.dreamsportslabs.guardian.dto.request;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_REQUEST;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
@NoArgsConstructor
public class V1AdminLogoutRequestDto {
  private String userId;

  public void validate() {
    if (StringUtils.isBlank(userId)) {
      throw INVALID_REQUEST.getCustomException("userId is required");
    }
  }
}
