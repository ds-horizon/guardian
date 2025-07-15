package com.dreamsportslabs.guardian.utils;

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

import java.util.List;

public final class ScopeUtil {
  ScopeUtil() {
    throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
  }

  public static void validateClaims(String scopeName, List<String> claims) {
    switch (scopeName) {
      case SCOPE_OPENID:
        if (claims.size() > 1 || !claims.contains(CLAIM_SUB)) {
          throw INVALID_REQUEST.getCustomException("openid scope must only include 'sub' claim");
        }
        break;
      case SCOPE_PHONE:
        if (claims.size() > 2
            || !claims.stream()
                .allMatch(c -> c.equals(CLAIM_PHONE_NUMBER) || c.equals(CLAIM_PHONE_VERIFIED))) {
          throw INVALID_REQUEST.getCustomException(
              "phone scope must include 'phone_number' or 'phone_number_verified' claim");
        }
        break;
      case SCOPE_EMAIL:
        if (claims.size() > 2
            || !claims.stream()
                .allMatch(c -> c.equals(CLAIM_EMAIL) || c.equals(CLAIM_EMAIL_VERIFIED))) {
          throw INVALID_REQUEST.getCustomException(
              "email scope must include 'email' or 'email_verified' claim");
        }
        break;
      case SCOPE_ADDRESS:
        if (claims.size() > 1 || !claims.contains(CLAIM_ADDRESS)) {
          throw INVALID_REQUEST.getCustomException(
              "address scope must only include 'address' claim");
        }
        break;
    }
  }
}
