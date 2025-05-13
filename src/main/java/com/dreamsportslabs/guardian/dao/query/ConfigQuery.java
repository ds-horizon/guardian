package com.dreamsportslabs.guardian.dao.query;

public class ConfigQuery {
  public static final String SMS_CONFIG =
      """
    SELECT is_ssl_enabled,
           host,
           port,
           send_sms_path,
           template_name,
           template_params
    FROM sms_config
    WHERE tenant_id = ?
    """;

  public static final String AUTH_CODE_CONFIG =
      """
    SELECT tenant_id,
           ttl,
           length
    FROM auth_code_config
    WHERE tenant_id = ?
    """;

  public static final String USER_CONFIG =
      """
    SELECT is_ssl_enabled,
           host,
           port,
           get_user_path,
           create_user_path,
           authenticate_user_path,
           add_provider_path
    FROM user_config
    WHERE tenant_id = ?
    """;

  public static final String EMAIL_CONFIG =
      """
    SELECT is_ssl_enabled,
           host,
           port,
           send_email_path,
           template_name,
           template_params
    FROM email_config
    WHERE tenant_id = ?
    """;

  public static final String FB_AUTH_CONFIG =
      """
    SELECT app_id,
           app_secret
    FROM fb_config
    WHERE tenant_id = ?
    """;

  public static final String GOOGLE_AUTH_CONFIG =
      """
        SELECT client_id,
               client_secret
        FROM google_config
        WHERE tenant_id = ?
        """;

  public static final String TOKEN_CONFIG =
      """
    SELECT algorithm,
           issuer,
           access_token_expiry,
           refresh_token_expiry,
           id_token_expiry,
           id_token_claims,
           rsa_keys
    FROM token_config
    WHERE tenant_id = ?
    """;

  public static final String OTP_CONFIG =
      """
    SELECT otp_length,
           try_limit,
           is_otp_mocked,
           resend_limit,
           otp_resend_interval,
           otp_validity,
           whitelisted_inputs
    FROM otp_config
    WHERE tenant_id = ?
    """;
}
