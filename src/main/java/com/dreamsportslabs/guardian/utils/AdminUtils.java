package com.dreamsportslabs.guardian.utils;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.INTERNAL_SERVER_ERROR;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.UNAUTHORIZED;

import com.dreamsportslabs.guardian.config.tenant.AdminConfig;
import com.dreamsportslabs.guardian.config.tenant.TenantConfig;
import com.dreamsportslabs.guardian.registry.Registry;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
@UtilityClass
public class AdminUtils {

  public static void validateAdminCredentials(
      String authorizationHeader, String tenantId, Registry registry) {
    if (StringUtils.isBlank(authorizationHeader)) {
      throw UNAUTHORIZED.getCustomException("Missing authorization header");
    }

    String[] credentials = Utils.getCredentialsFromAuthHeader(authorizationHeader);

    String username = credentials[0];
    String password = credentials[1];

    TenantConfig tenantConfig = registry.get(tenantId, TenantConfig.class);
    AdminConfig adminConfig = tenantConfig.getAdminConfig();

    if (adminConfig == null
        || StringUtils.isBlank(adminConfig.getUsername())
        || StringUtils.isBlank(adminConfig.getPassword())) {
      log.error("Admin configuration not found for tenant: {}", tenantId);
      throw INTERNAL_SERVER_ERROR.getCustomException("Admin configuration not available");
    }

    if (!username.equals(adminConfig.getUsername())
        || !password.equals(adminConfig.getPassword())) {
      log.warn("Invalid admin credentials for tenant: {}", tenantId);
      throw UNAUTHORIZED.getCustomException("Invalid admin credentials");
    }
  }
}
