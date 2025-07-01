package com.dreamsportslabs.guardian.dto.request.scope;

import static com.dreamsportslabs.guardian.constant.Constants.CLAIM_ADDRESS;
import static com.dreamsportslabs.guardian.constant.Constants.CLAIM_EMAIL;
import static com.dreamsportslabs.guardian.constant.Constants.CLAIM_EMAIL_VERIFIED;
import static com.dreamsportslabs.guardian.constant.Constants.CLAIM_PHONE_NUMBER;
import static com.dreamsportslabs.guardian.constant.Constants.CLAIM_PHONE_VERIFIED;
import static com.dreamsportslabs.guardian.constant.Constants.CLAIM_SUB;
import static com.dreamsportslabs.guardian.constant.Constants.SCOPE_ADDRESS;
import static com.dreamsportslabs.guardian.constant.Constants.SCOPE_EMAIL;
import static com.dreamsportslabs.guardian.constant.Constants.SCOPE_OPENID;
import static com.dreamsportslabs.guardian.constant.Constants.SCOPE_PHONE;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_REQUEST;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class CreateScopeRequestDto {
  private String name;
  private String displayName;
  private String description;
  private List<String> claims = new ArrayList<>();
  private String iconUrl;
  private boolean isOidc;

  public void validate() {
    if (StringUtils.isBlank(name)) {
      throw INVALID_REQUEST.getCustomException("scope name is required");
    }

    switch (name) {
      case SCOPE_OPENID:
        if (claims.size() > 1 || !claims.contains(CLAIM_SUB)) {
          throw INVALID_REQUEST.getCustomException("openid scope must include 'sub' claim");
        }
        break;
      case SCOPE_PHONE:
        if (claims.size() > 2
            || !claims.contains(CLAIM_PHONE_NUMBER)
            || !claims.contains(CLAIM_PHONE_VERIFIED)) {
          throw INVALID_REQUEST.getCustomException(
              "phone scope must include 'phone_number' and 'phone_number_verified' claim");
        }
        break;
      case SCOPE_EMAIL:
        if (claims.size() > 2
            || !claims.contains(CLAIM_EMAIL)
            || !claims.contains(CLAIM_EMAIL_VERIFIED)) {
          throw INVALID_REQUEST.getCustomException(
              "email scope must include 'email' and 'email_verified' claim");
        }
        break;
      case SCOPE_ADDRESS:
        if (claims.size() > 1 || !claims.contains(CLAIM_ADDRESS)) {
          throw INVALID_REQUEST.getCustomException("address scope must include 'address' claim");
        }
        break;
    }
  }
}
