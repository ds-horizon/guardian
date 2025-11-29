package com.dreamsportslabs.guardian.utils;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.INTERNAL_SERVER_ERROR;

import com.dreamsportslabs.guardian.constant.AuthMethod;
import com.dreamsportslabs.guardian.constant.AuthMethodCategory;
import com.dreamsportslabs.guardian.constant.MfaFactor;
import com.dreamsportslabs.guardian.dto.response.MfaFactorDto;
import io.vertx.core.json.JsonObject;
import java.util.ArrayList;
import java.util.List;

public class MfaFactorUtil {

  public static AuthMethodCategory getPrimaryCategory(List<AuthMethod> authMethods) {
    return authMethods.get(0).getCategory();
  }

  public static List<MfaFactor> getAvailableMfaFactors(AuthMethodCategory currentCategory) {
    List<MfaFactor> availableFactors = new ArrayList<>();

    for (MfaFactor factor : MfaFactor.values()) {
      AuthMethodCategory factorCategory = factor.getAuthMethod().getCategory();
      if (!factorCategory.equals(currentCategory)) {
        availableFactors.add(factor);
      }
    }
    return availableFactors;
  }

  public static boolean isFactorEnabled(MfaFactor factor, JsonObject user) {
    if (user == null) {
      return false;
    }

    return switch (factor) {
      case PASSWORD -> {
        Boolean isPasswordSet = user.getBoolean("isPasswordSet");
        yield isPasswordSet != null && isPasswordSet;
      }
      case PIN -> {
        Boolean isPinSet = user.getBoolean("isPinSet");
        yield isPinSet != null && isPinSet;
      }
      case SMS_OTP -> {
        String phoneNumber = user.getString("phoneNumber");
        yield phoneNumber != null && !phoneNumber.isBlank();
      }
      case EMAIL_OTP -> {
        String email = user.getString("email");
        yield email != null && !email.isBlank();
      }
      default -> false;
    };
  }

  public static List<MfaFactorDto> buildMfaFactors(
      List<AuthMethod> currentAuthMethods, JsonObject user, List<String> clientMfaEnabled) {
    if (currentAuthMethods == null || currentAuthMethods.isEmpty()) {
      throw INTERNAL_SERVER_ERROR.getCustomException(
          "AuthMethods cannot be null or empty when building MFA factors");
    }

    AuthMethodCategory currentCategory = getPrimaryCategory(currentAuthMethods);
    List<MfaFactor> availableFactors = getAvailableMfaFactors(currentCategory);

    List<MfaFactor> clientEnabledFactors = new ArrayList<>();
    if (clientMfaEnabled != null && !clientMfaEnabled.isEmpty()) {
      for (MfaFactor factor : availableFactors) {
        if (clientMfaEnabled.contains(factor.getValue())) {
          clientEnabledFactors.add(factor);
        }
      }
    } else {
      return new ArrayList<>();
    }

    List<MfaFactorDto> mfaFactors = new ArrayList<>();
    for (MfaFactor factor : clientEnabledFactors) {
      mfaFactors.add(
          MfaFactorDto.builder()
              .factor(factor.getValue())
              .isEnabled(isFactorEnabled(factor, user))
              .build());
    }
    return mfaFactors;
  }
}
