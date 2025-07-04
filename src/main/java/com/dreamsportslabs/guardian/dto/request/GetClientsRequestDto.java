package com.dreamsportslabs.guardian.dto.request;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_REQUEST;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.QueryParam;
import lombok.Data;

@Data
public class GetClientsRequestDto {

  @QueryParam("page")
  @DefaultValue("1")
  private int page;

  @QueryParam("pageSize")
  @DefaultValue("10")
  private int pageSize;

  public void validate() {
    if (page < 1) {
      throw INVALID_REQUEST.getCustomException("page value cannot be less than 1");
    }
    if (pageSize < 1 || pageSize > 100) {
      throw INVALID_REQUEST.getCustomException("pageSize must be between 1 and 100");
    }
  }
}
