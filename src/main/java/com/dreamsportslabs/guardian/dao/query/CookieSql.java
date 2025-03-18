package com.dreamsportslabs.guardian.dao.query;

public class CookieSql {
  public static final String SAVE_COOKIE =
      "INSERT INTO cookies (tenant_id, user_id, cookie_name, domain, path, same_site, cookie_value, cookie_exp, is_active, source, device_name, location, ip) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

  public static final String VALIDATE_COOKIE =
      "SELECT user_id FROM cookies WHERE tenant_id = ? AND cookie_value = ? AND is_active = 1 AND cookie_exp > UNIX_TIMESTAMP()";

  public static final String INVALIDATE_COOKIE =
      "UPDATE cookies SET is_active = 0 WHERE tenant_id = ? AND cookie_value = ?";

  public static final String INVALIDATE_ALL_COOKIES_FOR_USER =
      "UPDATE cookies SET is_active = 0 WHERE tenant_id = ? AND user_id = ?";
}
