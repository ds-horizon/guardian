package com.dreamsportslabs.guardian.config.tenant;

import jakarta.ws.rs.core.NewCookie;
import lombok.Data;

@Data
public class CookieConfig {
  private String cookieName;
  private NewCookie.SameSite sameSite;
  private Integer maxAge;
  private String domain;
  private String path;
}
