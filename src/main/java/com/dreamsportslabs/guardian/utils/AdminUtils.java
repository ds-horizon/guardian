package com.dreamsportslabs.guardian.utils;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.UNAUTHORIZED;

import com.dreamsportslabs.guardian.config.tenant.AdminConfig;
import com.dreamsportslabs.guardian.config.tenant.TenantConfig;
import com.dreamsportslabs.guardian.registry.Registry;
import java.util.Base64;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public class AdminUtils {

  public static void validateAdminCredentials(
      String authorizationHeader, String tenantId, Registry registry) {
    if (StringUtils.isBlank(authorizationHeader)) {
      throw UNAUTHORIZED.getCustomException("Missing authorization header");
    }

    String[] credentials = extractCredentialsFromAuthHeader(authorizationHeader);
    if (credentials.length != 2) {
      throw UNAUTHORIZED.getCustomException("Invalid authorization header format");
    }

    String username = credentials[0];
    String password = credentials[1];

    TenantConfig tenantConfig = registry.get(tenantId, TenantConfig.class);
    AdminConfig adminConfig = tenantConfig.getAdminConfig();

    if (adminConfig == null
        || StringUtils.isBlank(adminConfig.getUsername())
        || StringUtils.isBlank(adminConfig.getPassword())) {
      log.error("Admin configuration not found for tenant: {}", tenantId);
      throw UNAUTHORIZED.getCustomException("Admin configuration not available");
    }

    if (!username.equals(adminConfig.getUsername())
        || !password.equals(adminConfig.getPassword())) {
      log.warn("Invalid admin credentials for tenant: {}", tenantId);
      throw UNAUTHORIZED.getCustomException("Invalid admin credentials");
    }
  }

  private static String[] extractCredentialsFromAuthHeader(String authorizationHeader) {
    try {
      if (!authorizationHeader.startsWith("Basic ")) {
        throw UNAUTHORIZED.getCustomException("Invalid authorization header format");
      }

      String encodedCredentials = authorizationHeader.substring(6).trim();
      String decodedCredentials = new String(Base64.getDecoder().decode(encodedCredentials));
      return decodedCredentials.split(":", 2);
    } catch (Exception e) {
      throw UNAUTHORIZED.getCustomException("Invalid authorization header format");
    }
  }
}
