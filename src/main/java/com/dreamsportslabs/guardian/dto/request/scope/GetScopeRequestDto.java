package com.dreamsportslabs.guardian.dto.request.scope;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_REQUEST;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.QueryParam;
import java.util.List;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class GetScopeRequestDto {
  @QueryParam("name")
  private List<String> names;

  @QueryParam("page")
  @DefaultValue("1")
  private int page;

  @QueryParam("pageSize")
  @DefaultValue("10")
  private int pageSize;

  public void validate() {
    if (names != null && !names.isEmpty()) {
      for (String name : names) {
        if (StringUtils.isBlank(name)) {
          throw INVALID_REQUEST.getCustomException("scope name cannot be empty");
        }
      }
    }

    if (page < 1) {
      throw INVALID_REQUEST.getCustomException("page value cannot be less than 1");
    }
    if (pageSize < 1 || pageSize > 100) {
      throw INVALID_REQUEST.getCustomException("pageSize must be between 1 and 100");
    }
  }

  public boolean hasSpecificNames() {
    return names != null && !names.isEmpty();
  }
}
