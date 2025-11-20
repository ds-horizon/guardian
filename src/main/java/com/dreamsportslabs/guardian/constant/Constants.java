package com.dreamsportslabs.guardian.constant;

import com.google.common.collect.ImmutableList;
import io.vertx.core.json.JsonObject;
import java.util.ArrayList;

public final class Constants {
  public static final String APPLICATION_CONFIG = "application_config";
  public static final String TENANT_ID = "tenant-id";
  public static final String USERID = "userId";
  public static final String PROVIDER = "provider";
  public static final String IS_NEW_USER = "isNewUser";
  public static final String SCOPE = "scope";
  public static final String AES_ALGORITHM = "AES";
  public static final String AES_CBC_NO_PADDING = "AES/CBC/NoPadding";
  public static final String SHUTDOWN_STATUS = "__shutdown__";

  // HTTP Request Headers
  public static final String AUTHORIZATION = "Authorization";
  public static final String X_FORWARDED_FOR = "X-Forwarded-For";
  public static final String USER_AGENT = "User-Agent";
  public static final String BASIC_AUTHENTICATION_SCHEME = "Basic ";

  public static final String TOKEN = "token";
  public static final String CODE = "code";
  public static final String JWKS_KEYS = "keys";

  public static final String OIDC_TOKENS_ID_TOKEN = "id_token";
  public static final String OIDC_TOKENS_ACCESS_TOKEN = "access_token";

  public static final String OIDC_PROVIDERS_FACEBOOK = "facebook";
  public static final String OIDC_PROVIDERS_GOOGLE = "google";

  public static final String OIDC_CLAIMS_EMAIL = "email";
  public static final String OIDC_CLAIMS_SUB = "sub";
  public static final String OIDC_CLAIMS_FULL_NAME = "name";
  public static final String OIDC_CLAIMS_GIVEN_NAME = "given_name";
  public static final String OIDC_CLAIMS_FAMILY_NAME = "family_name";
  public static final String OIDC_CLAIMS_MIDDLE_NAME = "middle_name";
  public static final String OIDC_CLAIMS_PICTURE = "picture";
  public static final String OIDC_CLAIMS_PHONE = "phone_number";
  public static final String OIDC_USERID = "user_id";

  public static final String EXPIRY_OPTION_REDIS = "EX";
  public static final String EXPIRE_AT_REDIS = "EXAT";
  public static final String STATIC_OTP_NUMBER = "9";
  public static final String KEEP_TTL = "KEEPTTL";

  // Application config
  public static final String PORT = "port";
  public static final String MYSQL_WRITER_HOST = "mysql_writer_host";
  public static final String MYSQL_READER_HOST = "mysql_reader_host";
  public static final String MYSQL_DATABASE = "mysql_database";
  public static final String MYSQL_USER = "mysql_user";
  public static final String MYSQL_PASSWORD = "mysql_password";
  public static final String MYSQL_WRITER_MAX_POOL_SIZE = "mysql_writer_max_pool_size";
  public static final String MYSQL_READER_MAX_POOL_SIZE = "mysql_reader_max_pool_size";
  public static final String REDIS_HOST = "redis_host";
  public static final String REDIS_PORT = "redis_port";
  public static final String REDIS_TYPE = "redis_type";
  public static final String HTTP_CONNECT_TIMEOUT = "http_connect_timeout";
  public static final String HTTP_READ_TIMEOUT = "http_read_timeout";
  public static final String HTTP_WRITE_TIMEOUT = "http_write_timeout";
  public static final String TENANT_CONFIG_REFRESH_INTERVAL = "tenant_config_refresh_interval";
  public static final String HTTP_CLIENT_KEEP_ALIVE = "http_client_keep_alive";
  public static final String HTTP_CLIENT_KEEP_ALIVE_TIMEOUT = "http_client_keep_alive_timeout";
  public static final String HTTP_CLIENT_IDLE_TIMEOUT = "http_client_idle_timeout";
  public static final String HTTP_CLIENT_CONNECTION_POOL_MAX_SIZE =
      "http_client_connection_pool_max_size";
  public static final String APPLICATION_SHUTDOWN_GRACE_PERIOD =
      "application_shutdown_grace_period";

  // JWT CLAIMS
  public static final String JWT_CLAIMS_AUD = "aud";
  public static final String JWT_CLAIMS_CLIENT_ID = "client_id";
  public static final String JWT_CLAIMS_EXP = "exp";
  public static final String JWT_CLAIMS_IAT = "iat";
  public static final String JWT_CLAIMS_ISS = "iss";
  public static final String JWT_CLAIMS_JTI = "jti";
  public static final String JWT_CLAIMS_NONCE = "nonce";
  public static final String JWT_CLAIMS_RFT_ID = "rft_id";
  public static final String JWT_CLAIMS_SCOPE = "scope";
  public static final String JWT_CLAIMS_SUB = "sub";
  public static final String JWT_CLAIMS_AMR = "amr";

  // JWT Headers
  public static final String JWT_HEADERS_TYP = "typ";
  public static final String JWT_HEADERS_KID = "kid";

  // JWT Headers Values
  public static final String TYP_JWT_ACCESS_TOKEN = "at+jwt";

  // Response Headers
  public static final String CACHE_CONTROL_HEADER = "Cache-Control";
  public static final String PRAGMA_HEADER = "Pragma";
  public static final String WWW_AUTHENTICATE_HEADER = "WWW-Authenticate";

  public static final String APPLICATION_JWT = "application/jwt";

  // Response Header Values
  public static final String CACHE_CONTROL_NO_STORE = "no-store";
  public static final String PRAGMA_NO_CACHE = "no-cache";
  public static final String WWW_AUTHENTICATE_BASIC = "Basic realm=";
  public static final String JWT_TENANT_ID_CLAIM = "tid";

  public static final ImmutableList<String> fbAuthResponseTypes = ImmutableList.of(CODE, TOKEN);
  public static final ImmutableList<String> googleAuthResponseTypes = ImmutableList.of(CODE, TOKEN);
  public static final ImmutableList<String> passwordlessAuthResponseTypes =
      ImmutableList.of(CODE, TOKEN);
  public static final ImmutableList<String> registerResponseTypes = ImmutableList.of(CODE, TOKEN);
  public static final ImmutableList<String> loginResponseTypes = ImmutableList.of(CODE, TOKEN);

  public static final ArrayList<String> prohibitedForwardingHeaders = new ArrayList<>();

  static {
    prohibitedForwardingHeaders.add("CONTENT-LENGTH");
    prohibitedForwardingHeaders.add("ACCEPT");
    prohibitedForwardingHeaders.add("CONNECTION");
    prohibitedForwardingHeaders.add("HOST");
    prohibitedForwardingHeaders.add("CONTENT-TYPE");
    prohibitedForwardingHeaders.add("USER-AGENT");
    prohibitedForwardingHeaders.add("ACCEPT-ENCODING");
  }

  public static final String USER_FILTERS_EMAIL = "email";
  public static final String USER_FILTERS_PHONE = "phoneNumber";
  public static final String USER_FILTERS_PROVIDER_NAME = "providerName";
  public static final String USER_FILTERS_PROVIDER_USER_ID = "providerUserId";

  public static final String USER_RESPONSE_ADDITIONAL_CLAIMS = "additionalClaims";
  public static final String USER_RESPONSE_OIDC_ADDITIONAL_CLAIMS = "additional_claims";

  public static final String CACHE_KEY_CODE = "CODE";
  public static final String CACHE_KEY_STATE = "STATE";
  public static final String CACHE_KEY_AUTH_SESSION = "AUTH_SESSION";

  public static final String TOKEN_TYPE = "Bearer";

  public static final JsonObject NO_PICTURE = new JsonObject().put("data", new JsonObject());

  public static final String NEG_INF = "-inf";
  public static final Integer REVOCATIONS_FLOOR_FACTOR = 10;
  public static final Integer REVOCATIONS_FLOOR_FACTOR_2 = 60;
  public static final String REVOCATIONS_KEY_SEPARATOR = "_";
  public static final String REDIS_OPTION_BYSCORE = "BYSCORE";
  public static final Integer REVOCATIONS_KEY_APPLICATION_ID_INDEX = 0;
  public static final Integer REVOCATIONS_KEY_SCORE_START_INDEX = 1;
  public static final Integer REVOCATIONS_KEY_SCORE_END_INDEX = 2;
  public static final String REVOCATIONS_REDIS_KEY_PREFIX = "revocations";
  public static final Integer MILLIS_TO_SECONDS = 1000;

  public static final String ACCESS_TOKEN_COOKIE_NAME = "AT";
  public static final String REFRESH_TOKEN_COOKIE_NAME = "RT";
  public static final String SSO_TOKEN_COOKIE_NAME = "SSOT";

  public static final String SCOPE_EMAIL = "email";
  public static final String CLAIM_EMAIL = "email";
  public static final String SCOPE_PHONE = "phone";
  public static final String SCOPE_ADDRESS = "address";
  public static final String CLAIM_ADDRESS = "address";
  public static final String SCOPE_OPENID = "openid";
  public static final String CLAIM_EMAIL_VERIFIED = "email_verified";
  public static final String CLAIM_PHONE_VERIFIED = "phone_number_verified";
  public static final String CLAIM_PHONE_NUMBER = "phone_number";
  public static final String CLAIM_SUB = "sub";

  public static final String UNAUTHORIZED_ERROR_CODE = "unauthorized";
  public static final String OTP_RESEND_AFTER = "resendAfter";
  public static final String OTP_RETRIES_LEFT = "retriesLeft";
  public static final String ERROR = "error";
  public static final String ERROR_DESCRIPTION = "error_description";

  public static final String MESSAGE_CHANNEL = "channel";
  public static final String MESSAGE_TO = "to";
  public static final String MESSAGE_TEMPLATE_NAME = "templateName";
  public static final String MESSAGE_TEMPLATE_PARAMS = "templateParams";
  public static final String MESSAGE_TEMPLATE_PARAMS_OTP = "otp";

  public static final String FORMAT_PEM = "PEM";
  public static final String FORMAT_JWKS = "JWKS";
  public static final ImmutableList<Integer> VALID_KEY_SIZES = ImmutableList.of(2048, 3072, 4096);

  // OIDC Token Constants
  public static final String OIDC_REFRESH_TOKEN = "refresh_token";
  public static final String OIDC_GRANT_TYPE = "grant_type";
  public static final String OIDC_AUTHORIZATION_CODE = "authorization_code";
  public static final String OIDC_REDIRECT_URI = "redirect_uri";
  public static final String OIDC_CODE_VERIFIER = "code_verifier";
  public static final String OIDC_CODE = "code";
  public static final String OIDC_CLIENT_ID = "client_id";
  public static final String OIDC_CLIENT_SECRET = "client_secret";
  public static final String OIDC_NONCE = "nonce";

  public static final String APP_CODE = "code";
  public static final String APP_ACCESS_TOKEN = "accessToken";
  public static final String APP_REFRESH_TOKEN = "refreshToken";
  public static final String APP_ID_TOKEN = "idToken";
  public static final String APP_TOKEN_TYPE = "tokenType";
  public static final String APP_TOKEN_CODE_EXPIRY = "expiresIn";

  public static final String OIDC_PARAM_ERROR = "error";
  public static final String OIDC_PARAM_ERROR_DESCRIPTION = "error_description";
  public static final String OIDC_PARAM_STATE = "state";
  public static final String OIDC_PARAM_NONCE = "nonce";
  public static final String OIDC_PARAM_LOGIN_CHALLENGE = "login_challenge";
  public static final String OIDC_PARAM_CONSENT_CHALLENGE = "consent_challenge";
  public static final String OIDC_PARAM_LOGIN_HINT = "login_hint";
  public static final String OIDC_PARAM_PROMPT = "prompt";

  // WebAuthn Constants
  // State types
  public static final String WEBAUTHN_STATE_TYPE_ASSERT = "assert";
  public static final String WEBAUTHN_STATE_TYPE_ENROLL = "enroll";
  public static final String WEBAUTHN_RECOMMENDED_MODE_ASSERT = "assert";
  public static final String WEBAUTHN_RECOMMENDED_MODE_ENROLL = "enroll";

  // WebAuthn JSON keys
  public static final String WEBAUTHN_KEY_CHALLENGE = "challenge";
  public static final String WEBAUTHN_KEY_RP_ID = "rpId";
  public static final String WEBAUTHN_KEY_USER_VERIFICATION = "userVerification";
  public static final String WEBAUTHN_KEY_TIMEOUT = "timeout";
  public static final String WEBAUTHN_KEY_ALLOW_CREDENTIALS = "allowCredentials";
  public static final String WEBAUTHN_KEY_TYPE = "type";
  public static final String WEBAUTHN_KEY_ID = "id";
  public static final String WEBAUTHN_KEY_RP = "rp";
  public static final String WEBAUTHN_KEY_USER = "user";
  public static final String WEBAUTHN_KEY_NAME = "name";
  public static final String WEBAUTHN_KEY_DISPLAY_NAME = "displayName";
  public static final String WEBAUTHN_KEY_PUB_KEY_CRED_PARAMS = "pubKeyCredParams";
  public static final String WEBAUTHN_KEY_AUTHENTICATOR_SELECTION = "authenticatorSelection";
  public static final String WEBAUTHN_KEY_ATTESTATION = "attestation";
  public static final String WEBAUTHN_KEY_EXCLUDE_CREDENTIALS = "excludeCredentials";
  public static final String WEBAUTHN_KEY_AUTHENTICATOR_ATTACHMENT = "authenticatorAttachment";
  public static final String WEBAUTHN_KEY_RESIDENT_KEY = "residentKey";
  public static final String WEBAUTHN_KEY_ALG = "alg";
  public static final String WEBAUTHN_KEY_TRANSPORTS = "transports";
  public static final String WEBAUTHN_JSON_KEY_USERNAME = "username";
  public static final String WEBAUTHN_JSON_KEY_CHALLENGE = "challenge";
  public static final String WEBAUTHN_JSON_KEY_WEBAUTHN = "webauthn";
  public static final String WEBAUTHN_JSON_KEY_ORIGIN = "origin";
  public static final String WEBAUTHN_JSON_KEY_DOMAIN = "domain";
  public static final String WEBAUTHN_JSON_KEY_ID = "id";
  public static final String WEBAUTHN_JSON_KEY_RAW_ID = "rawId";
  public static final String WEBAUTHN_JSON_KEY_TYPE = "type";
  public static final String WEBAUTHN_JSON_KEY_RESPONSE = "response";
  public static final String WEBAUTHN_JSON_KEY_ATTESTATION_OBJECT = "attestationObject";
  public static final String WEBAUTHN_JSON_KEY_CLIENT_DATA_JSON = "clientDataJSON";
  public static final String WEBAUTHN_JSON_KEY_AUTHENTICATOR_DATA = "authenticatorData";
  public static final String WEBAUTHN_JSON_KEY_SIGNATURE = "signature";
  public static final String WEBAUTHN_JSON_KEY_USER_HANDLE = "userHandle";
  public static final String WEBAUTHN_JSON_KEY_UV = "uv";
  public static final String WEBAUTHN_JSON_KEY_TRANSPORT = "transport";
  public static final String WEBAUTHN_JSON_KEY_AAGUID = "aaguid";
  public static final String WEBAUTHN_JSON_KEY_ACCESS_TOKEN = "access_token";
  public static final String WEBAUTHN_JSON_KEY_REFRESH_TOKEN = "refresh_token";
  public static final String WEBAUTHN_JSON_KEY_ID_TOKEN = "id_token";
  public static final String WEBAUTHN_JSON_KEY_TOKEN_TYPE = "token_type";
  public static final String WEBAUTHN_JSON_KEY_EXPIRES_IN = "expires_in";

  // WebAuthn values
  public static final String WEBAUTHN_VALUE_PUBLIC_KEY = "public-key";
  public static final String WEBAUTHN_VALUE_REQUIRED = "required";
  public static final String WEBAUTHN_VALUE_PREFERRED = "preferred";
  public static final String WEBAUTHN_VALUE_PLATFORM = "platform";
  public static final String WEBAUTHN_VALUE_ATTESTATION_DIRECT = "direct";

  // AAGUID policy mode values
  public static final String WEBAUTHN_AAGUID_POLICY_MODE_ANY = "any";
  public static final String WEBAUTHN_AAGUID_POLICY_MODE_ALLOWLIST = "allowlist";
  public static final String WEBAUTHN_AAGUID_POLICY_MODE_MDS_ENFORCED = "mds_enforced";

  // WebAuthn error messages
  public static final String WEBAUTHN_ERROR_MISSING_AUTH_HEADER = "Missing Authorization header";
  public static final String WEBAUTHN_ERROR_INVALID_REFRESH_TOKEN = "Invalid refresh token";
  public static final String WEBAUTHN_ERROR_REFRESH_TOKEN_EXPIRED = "Refresh token expired";
  public static final String WEBAUTHN_ERROR_NOT_CONFIGURED =
      "WebAuthn not configured for this client";
  public static final String WEBAUTHN_ERROR_INVALID_STATE = "Invalid or expired state";
  public static final String WEBAUTHN_ERROR_MFA_REQUIRED =
      "MFA is mandatory and refresh token has only one AMR";
  public static final String WEBAUTHN_ERROR_VERIFICATION_FAILED = "WebAuthn verification failed";
  public static final String WEBAUTHN_ERROR_INVALID_STATE_TYPE = "Invalid state type: %s";
  public static final String WEBAUTHN_ERROR_CREDENTIAL_NOT_FOUND =
      "Credential not found for assertion";
  public static final String WEBAUTHN_ERROR_CLIENT_NOT_FOUND = "Client not found";
  public static final String WEBAUTHN_ERROR_USER_VERIFICATION_REQUIRED =
      "User verification required but not performed";
  public static final String WEBAUTHN_ERROR_INVALID_TRANSPORT = "Invalid transport used";
  public static final String WEBAUTHN_ERROR_STATE_EXPIRED = "State has expired";
  public static final String WEBAUTHN_ERROR_STATE_USER_MISMATCH =
      "State user ID does not match refresh token user ID";
  public static final String WEBAUTHN_ERROR_STATE_CLIENT_MISMATCH =
      "State client ID does not match request client ID";
  public static final String WEBAUTHN_ERROR_DUPLICATE_CREDENTIAL = "Credential already exists";
  public static final String WEBAUTHN_ERROR_SIGN_COUNT_REPLAY =
      "Sign count validation failed - possible replay attack";

  // Client data types
  public static final String WEBAUTHN_CLIENT_DATA_TYPE_CREATE = "webauthn.create";

  // MFA policy values
  public static final String WEBAUTHN_MFA_POLICY_MANDATORY = "mandatory";

  // Binding type
  public static final String WEBAUTHN_BINDING_TYPE_WEBAUTHN = "webauthn";
}
