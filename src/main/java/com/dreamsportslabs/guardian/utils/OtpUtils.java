package com.dreamsportslabs.guardian.utils;

import com.dreamsportslabs.guardian.config.tenant.EmailConfig;
import com.dreamsportslabs.guardian.config.tenant.SmsConfig;
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

  public static void updateContactTemplate(
      SmsConfig smsConfig, EmailConfig emailConfig, Contact contact) {
    if (contact.getTemplate() != null) {
      return;
    }

    if (contact.getChannel() == Channel.EMAIL) {
      contact.setTemplate(
          new Template(emailConfig.getTemplateName(), emailConfig.getTemplateParams()));
    }

    if (contact.getChannel() == Channel.SMS) {
      contact.setTemplate(new Template(smsConfig.getTemplateName(), smsConfig.getTemplateParams()));
    }
  }
}
