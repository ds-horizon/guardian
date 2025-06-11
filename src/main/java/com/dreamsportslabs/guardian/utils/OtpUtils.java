package com.dreamsportslabs.guardian.utils;

import com.dreamsportslabs.guardian.config.tenant.TenantConfig;
import com.dreamsportslabs.guardian.constant.Channel;
import com.dreamsportslabs.guardian.constant.Contact;
import com.dreamsportslabs.guardian.constant.Template;
import org.apache.commons.lang3.RandomStringUtils;

public final class OtpUtils {

  private OtpUtils() {
    throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
  }

  public static String generateState() {
    return RandomStringUtils.randomAlphanumeric(10);
  }

  public static void updateContactTemplate(TenantConfig tenantConfig, Contact contact) {
    if (contact.getTemplate() != null) {
      return;
    }

    if (contact.getChannel() == Channel.EMAIL) {
      contact.setTemplate(
          new Template(
              tenantConfig.getEmailConfig().getTemplateName(),
              tenantConfig.getEmailConfig().getTemplateParams()));
    }
    if (contact.getChannel() == Channel.SMS) {
      contact.setTemplate(
          new Template(
              tenantConfig.getSmsConfig().getTemplateName(),
              tenantConfig.getSmsConfig().getTemplateParams()));
    }
  }
}
