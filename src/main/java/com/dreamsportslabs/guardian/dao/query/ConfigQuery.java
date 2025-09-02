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
           app_secret,
           send_app_secret
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
           rsa_keys,
           cookie_same_site,
           cookie_path,
           cookie_domain,
           cookie_secure,
           cookie_http_only,
           additional_claims_enabled
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

  public static final String CONTACT_VERIFY_CONFIG =
      """
       SELECT otp_length,
              try_limit,
              is_otp_mocked,
              resend_limit,
              otp_resend_interval,
              otp_validity,
              whitelisted_inputs
       FROM contact_verify_config
       WHERE tenant_id = ?
    """;

  public static final String OIDC_PROVIDER_CONFIG =
      """
    SELECT tenant_id,
           provider_name,
           issuer,
           jwks_url,
           token_url,
           client_id,
           client_secret,
           redirect_uri,
           client_auth_method,
           is_ssl_enabled,
           user_identifier,
           audience_claims
    FROM oidc_provider_config
    WHERE tenant_id = ?
    """;

  public static final String ADMIN_CONFIG =
      """
    SELECT username,
           password
    FROM admin_config
    WHERE tenant_id = ?
    """;

  public static final String OIDC_CONFIG =
      """
    SELECT tenant_id,
           issuer,
           authorization_endpoint,
           token_endpoint,
           userinfo_endpoint,
           revocation_endpoint,
           jwks_uri,
           login_page_uri,
           consent_page_uri,
           authorize_ttl,
           grant_types_supported,
           response_types_supported,
           subject_types_supported,
           id_token_signing_alg_values_supported,
           token_endpoint_auth_methods_supported
    FROM oidc_config
    WHERE tenant_id = ?
    """;
}
