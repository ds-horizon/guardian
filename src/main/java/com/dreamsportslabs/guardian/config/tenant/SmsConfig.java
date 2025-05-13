package com.dreamsportslabs.guardian.config.tenant;

import java.util.HashMap;
import lombok.Data;

@Data
public class SmsConfig {
  private String host;
  private int port;
  private String sendSmsPath;
  private boolean isSslEnabled;
  private String templateName;
  private HashMap<String, String> templateParams;
}
