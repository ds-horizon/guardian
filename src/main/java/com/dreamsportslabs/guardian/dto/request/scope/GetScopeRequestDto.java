package com.dreamsportslabs.guardian.dto.request.scope;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_REQUEST;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.QueryParam;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class GetScopeRequestDto {
  @QueryParam("name")
  private String name;

  @QueryParam("page")
  @DefaultValue("1")
  private int page;

  @QueryParam("pageSize")
  @DefaultValue("10")
  private int pageSize;

  public void validate() {
    if (name != null && StringUtils.isBlank(name)) {
      throw INVALID_REQUEST.getCustomException("scope name cannot be empty");
    }

    if (page <= 1) {
      page = 1;
    }
    if (pageSize < 1 || pageSize > 100) {
      pageSize = 10;
    }
  }
}
