package com.dreamsportslabs.guardian.dto.request;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_REQUEST;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class V1GuestLoginRequestDto {
  @JsonProperty("client_id")
  private String clientId;

  @JsonProperty("guest_identifier")
  private String guestIdentifier;

  @JsonProperty("scopes")
  private List<String> scopes;

  public void validate() {
    if (StringUtils.isBlank(guestIdentifier)) {
      throw INVALID_REQUEST.getCustomException("guestIdentifier cannot be null or empty");
    }
    if (StringUtils.isBlank(clientId)) {
      throw INVALID_REQUEST.getCustomException("clientId cannot be null or empty");
    }
    if (scopes == null || scopes.isEmpty()) {
      throw INVALID_REQUEST.getCustomException("scopes cannot be null or empty");
    }
  }
}
