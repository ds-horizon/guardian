package com.dreamsportslabs.guardian;

public class Constants {
  // Header Params
  public static final String HEADER_TENANT_ID = "tenant-id";

  // Request Body Params
  public static final String BODY_PARAM_USERNAME = "username";
  public static final String BODY_PARAM_PASSWORD = "password";
  public static final String BODY_PARAM_REFRESH_TOKEN = "refreshToken";
  public static final String BODY_PARAM_RESPONSE_TYPE = "responseType";
  public static final String BODY_PARAM_RESPONSE_TYPE_TOKEN = "token";
  public static final String BODY_PARAM_FLOW = "flow";
  public static final String BODY_PARAM_CONTACTS = "contacts";
  public static final String BODY_PARAM_CONTACT = "contact";
  public static final String BODY_PARAM_META_INFO = "metaInfo";
  public static final String BODY_PARAM_ADDITIONAL_INFO = "additionalInfo";
  public static final String BODY_PARAM_STATE = "state";
  public static final String BODY_PARAM_CHANNEL = "channel";
  public static final String BODY_PARAM_TRIES = "tries";
  public static final String BODY_PARAM_RESENDS = "resends";
  public static final String BODY_PARAM_RESENDS_LEFT = "resendsLeft";
  public static final String BODY_PARAM_RESEND_AFTER = "resendsAfter";
  public static final String BODY_PARAM_RESEND_INTERVAL = "resendInterval";
  public static final String BODY_PARAM_RETRIES_LEFT = "retriesLeft";
  public static final String BODY_PARAM_OTP_MOCKED = "isOtpMocked";
  public static final String BODY_PARAM_MAX_TRIES = "maxTries";
  public static final String BODY_PARAM_MAX_RESENDS = "maxResends";
  public static final String BODY_PARAM_EXPIRY = "expiry";
  public static final String BODY_PARAM_IDENTIFIER = "identifier";
  public static final String BODY_PARAM_TEMPLATE = "template";
  public static final String BODY_PARAM_NAME = "name";
  public static final String BODY_PARAM_USERID = "userId";
  public static final String BODY_PARAM_PHONE_NUMBER = "phoneNumber";
  public static final String BODY_PARAM_EMAIL = "email";
  public static final String BODY_PARAM_DEVICE_NAME = "deviceName";
  public static final String BODY_PARAM_LOCATION = "location";
  public static final String BODY_PARAM_PARAMS = "params";
  public static final String BODY_PARAM_IS_NEW_USER = "isNewUser";
  public static final String BODY_PARAM_LOGIN_CHALLENGE = "loginChallenge";
  public static final String BODY_PARAM_CONSENT_CHALLENGE = "consentChallenge";
  public static final String BODY_PARAM_CONSENTED_SCOPES = "consentedScopes";

  // User Block Flow Constants
  public static final String BODY_PARAM_USER_IDENTIFIER = "userIdentifier";
  public static final String BODY_PARAM_BLOCK_FLOWS = "blockFlows";
  public static final String BODY_PARAM_REASON = "reason";
  public static final String BODY_PARAM_UNBLOCKED_AT = "unblockedAt";
  public static final String BODY_PARAM_UNBLOCK_FLOWS = "unblockFlows";
  public static final String RESPONSE_BODY_PARAM_BLOCKED_FLOWS = "blockedFlows";
  public static final String RESPONSE_BODY_PARAM_UNBLOCKED_FLOWS = "unblockedFlows";
  public static final String RESPONSE_BODY_PARAM_TOTAL_COUNT = "totalCount";

  // Scope Configuration Params
  public static final String BODY_PARAM_SCOPE = "name";
  public static final String BODY_PARAM_DISPLAY_NAME = "displayName";
  public static final String BODY_PARAM_DESCRIPTION = "description";
  public static final String BODY_PARAM_CLAIMS = "claims";
  public static final String BODY_PARAM_ICON_URL = "iconUrl";
  public static final String BODY_PARAM_IS_OIDC = "isOidc";
  public static final String BODY_PARAM_OTP = "otp";

  public static final String BODY_CHANNEL_EMAIL = "EMAIL";
  public static final String BODY_CHANNEL_SMS = "SMS";

  public static final String TEST_SCOPE_NAME = "Test Scope";
  public static final String TEST_DESCRIPTION = "Test description";
  public static final String TEST_DISPLAY_NAME = "Test display name";
  public static final String TEST_ICON_URL = "https://example.com/icon.png";
  public static final String TEST_EMAIL_CLAIM = "email";
  public static final String TEST_EMAIL_VERIFIED_CLAIM = "email_verified";
  public static final String TEST_NAME_CLAIM = "name";
  public static final String TEST_PICTURE_CLAIM = "picture";
  public static final String TEST_PHONE_CLAIM = "phone";
  public static final String TEST_PHONE_VERIFIED_CLAIM = "phone_verified";

  // Predefined scope names
  public static final String SCOPE = "scope";
  public static final String SCOPE_OPENID = "openid";
  public static final String SCOPE_PHONE = "phone";
  public static final String SCOPE_EMAIL = "email";
  public static final String SCOPE_ADDRESS = "address";
  public static final String DISPLAY_NAME = "displayName";

  // Predefined claim names
  public static final String CLAIM_SUB = "sub";
  public static final String CLAIM_PHONE_NUMBER = "phone_number";
  public static final String CLAIM_PHONE_NUMBER_VERIFIED = "phone_number_verified";
  public static final String CLAIM_EMAIL_VERIFIED = "email_verified";
  public static final String CLAIM_ADDRESS = "address";
  public static final String CLAIM_EMAIL = "email";

  // Test scope display names
  public static final String TEST_OPENID_SCOPE_DISPLAY_NAME = "OpenID Scope";
  public static final String TEST_PHONE_SCOPE_DISPLAY_NAME = "Phone Scope";
  public static final String TEST_EMAIL_SCOPE_DISPLAY_NAME = "Email Scope";
  public static final String TEST_ADDRESS_SCOPE_DISPLAY_NAME = "Address Scope";

  // Test scope descriptions
  public static final String TEST_OPENID_SCOPE_DESCRIPTION = "OpenID Connect scope";
  public static final String TEST_PHONE_SCOPE_DESCRIPTION = "Phone number scope";
  public static final String TEST_EMAIL_SCOPE_DESCRIPTION = "Email scope";
  public static final String TEST_ADDRESS_SCOPE_DESCRIPTION = "Address scope";

  // Other test constants
  public static final String TEST_DUPLICATE_SCOPE_DISPLAY_NAME = "Duplicate Scope";
  public static final String TEST_MULTIPLE_CLAIMS_SCOPE_DISPLAY_NAME = "Multiple Claims Scope";
  public static final String TEST_EXTRA_CLAIM = "extra_claim";

  // Update scope test constants
  public static final String TEST_UPDATED_DISPLAY_NAME = "Updated Display Name";
  public static final String TEST_UPDATED_DESCRIPTION = "Updated description for testing";
  public static final String TEST_UPDATED_ICON_URL = "https://example.com/updated-icon.png";
  public static final String TEST_UPDATED_CLAIM = "updated_claim";
  public static final String TEST_PARTIAL_UPDATE_DISPLAY_NAME = "Partially Updated Scope";

  public static final String ERROR_CODE_SCOPE_NOT_FOUND = "scope_not_found";
  public static final String ERROR_MSG_SCOPE_NOT_FOUND = "Scope not found";
  public static final String ERROR_MSG_NO_UPDATES_PROVIDED = "No updates provided for scope";

  public static final String TENANT_1 = "tenant1";
  public static final String TENANT_2 = "tenant2";

  public static final String ERROR_MSG_SCOPE_REQUIRED = "scope name is required";
  public static final String ERROR_MSG_SCOPE_CANNOT_BE_EMPTY = "scope name cannot be empty";
  public static final String ERROR_CODE_SCOPE_ALREADY_EXISTS = "scope_already_exists";
  public static final String ERROR_MSG_SCOPE_ALREADY_EXISTS = "scope already exists for tenant";
  public static final String ERROR_MSG_OPENID_SCOPE_INVALID_CLAIMS =
      "openid scope must only include 'sub' claim";
  public static final String ERROR_MSG_PHONE_SCOPE_INVALID_CLAIMS =
      "phone scope must include 'phone_number' or 'phone_number_verified' claim";
  public static final String ERROR_MSG_EMAIL_SCOPE_INVALID_CLAIMS =
      "email scope must include 'email' or 'email_verified' claim";
  public static final String ERROR_MSG_ADDRESS_SCOPE_INVALID_CLAIMS =
      "address scope must only include 'address' claim";
  public static final String ERROR_MSG_PAGE_VALUE_CANNOT_BE_LESS_THAN_1 =
      "page value cannot be less than 1";
  public static final String ERROR_MSG_PAGE_SIZE_VALUE_CANNOT_BE_LESS_THAN_1 =
      "pageSize must be between 1 and 100";

  public static final String QUERY_PARAM_PAGE = "page";
  public static final String QUERY_PARAM_PAGE_SIZE = "pageSize";
  public static final String QUERY_PARAM_NAME = "name";

  public static final String PASSWORDLESS_FLOW_SIGNINUP = "SIGNINUP";
  public static final String PASSWORDLESS_FLOW_SIGNUP = "SIGNUP";
  public static final String PASSWORDLESS_FLOW_SIGNIN = "SIGNIN";

  public static final String JWT_HEADER_KID = "kid";
  public static final String JWT_HEADER_ALG = "alg";

  public static final String JWT_CLAIM_IAT = "iat";
  public static final String JWT_CLAIM_CLIENT_ID = "client_id";
  public static final String JWT_CLAIM_JTI = "jti";
  public static final String JWT_CLAIM_SCOPE = "scope";
  public static final String JWT_CLAIM_EXP = "exp";
  public static final String JWT_CLAIM_ISS = "iss";
  public static final String JWT_CLAIM_SUB = "sub";
  public static final String JWT_CLAIM_RFT_ID = "rft_id";

  // Test Constants for OIDC Client Management
  public static final String TENANT_ID_HEADER = "tenant-id";

  // Error Response
  public static final String ERROR = "error";
  public static final String CODE = "code";
  public static final String MESSAGE = "message";
  public static final String METADATA = "metadata";
  public static final String ERROR_INCORRECT_OTP = "incorrect_otp";
  public static final String ERROR_INVALID_REQUEST = "invalid_request";
  public static final String ERROR_USER_NOT_EXISTS = "user_not_exists";
  public static final String ERROR_USER_EXISTS = "user_exists";
  public static final String ERROR_RESENDS_NOT_ALLOWED = "resends_not_allowed";
  public static final String ERROR_INVALID_STATE = "invalid_state";
  public static final String ERROR_RESENDS_EXHAUSTED = "resends_exhausted";
  public static final String ERROR_RETRIES_EXHAUSTED = "retries_exhausted";
  public static final String ERROR_SMS_SERVICE = "sms_service_error";
  public static final String INVALID_STATE = "invalid_state";
  public static final String ERROR_FLOW_BLOCKED = "flow_blocked";
  public static final String ERROR_INTERNAL_ERROR = "internal_error";
  public static final String ERROR_INTERNAL_SERVER_ERROR = "internal_server_error";

  public static final String RESPONSE_BODY_PARAM_STATE = "state";
  public static final String RESPONSE_BODY_PARAM_TRIES = "tries";
  public static final String RESPONSE_BODY_PARAM_RETRIES_LEFT = "retriesLeft";
  public static final String RESPONSE_BODY_PARAM_RESENDS = "resends";
  public static final String RESPONSE_BODY_PARAM_RESENDS_LEFT = "resendsLeft";
  public static final String RESPONSE_BODY_PARAM_RESEND_AFTER = "resendAfter";
  public static final String RESPONSE_BODY_PARAM_IS_NEW_USER = "isNewUser";
  public static final String RESPONSE_BODY_PARAM_RETRIES_LEFT_METADATA = "retriesLeft";

  public static final String PASSWORDLESS_MODEL_IS_NEW_USER = "isNewUser";
  public static final String PASSWORDLESS_MODEL_TRIES = "tries";
  public static final String PASSWORDLESS_MODEL_RESENDS = "resends";
  public static final String PASSWORDLESS_MODEL_RESEND_AFTER = "resendAfter";
  public static final String PASSWORDLESS_MODEL_STATE = "state";
  public static final String PASSWORDLESS_MODEL_IS_OTP_MOCKED = "isOtpMocked";
  public static final String PASSWORDLESS_MODEL_FLOW = "flow";
  public static final String PASSWORDLESS_MODEL_RESPONSE_TYPE = "responseType";
  public static final String PASSWORDLESS_MODEL_EXPIRY = "expiry";
  public static final String PASSWORDLESS_MODEL_CREATED_AT_EPOCH = "createdAtEpoch";
  public static final String PASSWORDLESS_MODEL_USER = "user";
  public static final String PASSWORDLESS_MODEL_CONTACTS = "contacts";
  public static final String PASSWORDLESS_MODEL_CONTACTS_TEMPLATE = "template";
  public static final String PASSWORDLESS_MODEL_CONTACTS_TEMPLATE_NAME = "name";

  // RSA Key Generation
  public static final String TENANT_VALID = "tenant1";
  public static final String RSA_KEY_KID = "kid";
  public static final String RSA_KEY_PUBLIC_KEY = "publicKey";
  public static final String RSA_KEY_PRIVATE_KEY = "privateKey";
  public static final String RSA_KEY_SIZE = "keySize";
  public static final String RSA_KEY_FORMAT = "format";
  public static final String RSA_KEY_TYPE = "kty";
  public static final String RSA_KEY_USE = "use";
  public static final String RSA_KEY_MODULUS = "n";
  public static final String RSA_KEY_EXPONENT = "e";
  public static final int RSA_KEY_SIZE_2048 = 2048;
  public static final int RSA_KEY_SIZE_3072 = 3072;
  public static final int RSA_KEY_SIZE_4096 = 4096;
  public static final int RSA_KEY_SIZE_INVALID = 1024;
  public static final int RSA_PUBLIC_EXPONENT = 65537;
  public static final int RSA_4096_MIN_LENGTH = 3000;
  public static final int RSA_KID_MIN_LENGTH = 10;
  public static final String RSA_FORMAT_PEM = "PEM";
  public static final String RSA_FORMAT_JWKS = "JWKS";
  public static final String RSA_FORMAT_INVALID = "INVALID";
  public static final String RSA_FORMAT_EMPTY = "";
  public static final String RSA_KEY_TYPE_RSA = "RSA";
  public static final String RSA_KEY_USE_SIG = "sig";
  public static final String RSA_KEY_EXPONENT_AQAB = "AQAB";
  public static final String RSA_ALGORITHM = "RSA";
  public static final String PEM_PUBLIC_KEY_HEADER = "-----BEGIN PUBLIC KEY-----";
  public static final String PEM_PUBLIC_KEY_FOOTER = "-----END PUBLIC KEY-----";
  public static final String PEM_PRIVATE_KEY_HEADER = "-----BEGIN PRIVATE KEY-----";
  public static final String PEM_PRIVATE_KEY_FOOTER = "-----END PRIVATE KEY-----";
  public static final String ERROR_MSG_INVALID_RSA_KEY_LENGTH =
      "Invalid RSA key length. Allowed values are [2048, 3072, 4096]";
  public static final String ERROR_MSG_INVALID_KEY_FORMAT =
      "Invalid key format. Allowed values are PEM or JWKS";

  public static final String ASSERT_PUBLIC_KEY_MODULUS_2048 =
      "Public key modulus should be 2048 bits";
  public static final String ASSERT_PRIVATE_KEY_MODULUS_2048 =
      "Private key modulus should be 2048 bits";
  public static final String ASSERT_KEYS_SAME_MODULUS =
      "Public and private keys should have same modulus";
  public static final String ASSERT_PUBLIC_EXPONENT_65537 = "Public exponent should be 65537";

  // Constants for OIDC Client Management
  public static final String CLIENT_ID = "client_id";
  public static final String CLIENT_NAME = "client_name";
  public static final String CLIENT_URI = "client_uri";
  public static final String CLIENT_SECRET = "client_secret";
  public static final String REDIRECT_URIS = "redirect_uris";
  public static final String CONTACTS = "contacts";
  public static final String GRANT_TYPES = "grant_types";
  public static final String RESPONSE_TYPES = "response_types";
  public static final String LOGO_URI = "logo_uri";
  public static final String POLICY_URI = "policy_uri";
  public static final String SKIP_CONSENT = "skip_consent";
  public static final String PAGE = "page";
  public static final String PAGE_SIZE = "pageSize";
  public static final String CLIENTS = "clients";
  public static final String EXAMPLE_COM = "https://example.com";
  public static final String EXAMPLE_CALLBACK = "https://example.com/callback";
  public static final String EXAMPLE_LOGO = "https://example.com/logo.png";
  public static final String EXAMPLE_POLICY = "https://example.com/policy";
  public static final String ADMIN_EMAIL = "admin@example.com";
  public static final String SUPPORT_EMAIL = "support@example.com";
  public static final String DEV_EMAIL = "dev@example.com";
  public static final String UPDATED_EXAMPLE_COM = "https://updated-example.com";
  public static final String NEW_URI_EXAMPLE = "https://new-uri.example.com";
  public static final String CALLBACK_1 = "https://example.com/callback1";
  public static final String CALLBACK_2 = "https://example.com/callback2";
  public static final String CALLBACK_3 = "https://example.com/callback3";
  public static final String AUTHORIZATION_CODE = "authorization_code";
  public static final String REFRESH_TOKEN = "refresh_token";
  public static final String CLIENT_CREDENTIALS = "client_credentials";
  public static final String CLIENT_NAME_REQUIRED = "Client name is required";
  public static final String CLIENT_ALREADY_EXISTS = "client_already_exists";
  public static final String CLIENT_NOT_FOUND = "client_not_found";
  public static final String INVALID_REQUEST = "invalid_request";
  public static final String NO_FIELDS_TO_UPDATE = "no_fields_to_update";
  public static final String ERROR_MSG_NO_FIELDS_TO_UPDATE = "No fields for update";

  public static final String CLIENT_NOT_FOUND_MSG = "Client not found";
  public static final String GRANT_TYPES_REQUIRED = "Grant types are required";
  public static final String REDIRECT_URIS_REQUIRED = "Redirect URIs are required";
  public static final String RESPONSE_TYPES_REQUIRED = "Response types are required";
  public static final String CLIENT_NAME_BLANK = "Client name cannot be blank";
  public static final String PAGE_VALUE_ERROR = "page value cannot be less than 1";
  public static final String PAGE_SIZE_ERROR = "pageSize must be between 1 and 100";
  public static final String INVALID_GRANT_TYPES_MSG =
      "The value provided for the field is invalid or does not exist: grant_types";
  public static final String INVALID_RESPONSE_TYPES_MSG =
      "The value provided for the field is invalid or does not exist: response_types";
  public static final String TEST_CLIENT_PREFIX = "Test Client ";
  public static final String MINIMAL_CLIENT_PREFIX = "Minimal Client ";
  public static final String UPDATED_CLIENT_NAME = "Updated Client Name";
  public static final String UPDATED_NAME_ONLY = "Updated Name Only";
  public static final String HACKED_NAME = "Hacked Name";
  public static final String INVALID_GRANT_TYPE = "invalid_grant_type";
  public static final String INVALID_RESPONSE_TYPE = "invalid_response_type";
  public static final String INVALID_TENANT = "invalid";
  public static final String INVALID_CODE = "invalid_code";
  public static final String BLANK_STRING = "   ";
  public static final int MIN_SECRET_LENGTH = 32;
  public static final int MIN_CLIENT_ID_LENGTH = 20;

  public static final int LONG_NAME_LENGTH = 99;
  public static final int VERY_LONG_TENANT_LENGTH = 100;

  // Constants for OIDC Client Scope Management
  public static final String SCOPES = "scopes";
  public static final String SCOPE_ALREADY_EXISTS = "scope_already_exists";
  public static final String SCOPE_REQUIRED = "Scope is required";
  public static final String NO_VALID_SCOPES = "No valid scopes found";
  public static final String SOME_SCOPES_NOT_EXIST = "Some scopes do not exist";
  public static final String SCOPE_ALREADY_EXISTS_MSG = "Scope already exists for client";

  // Authorization Test Constants
  public static final String TEST_STATE = "test_state_123";
  public static final String TEST_LOGIN_HINT = "user@example.com";
  public static final String TEST_CODE_CHALLENGE = "E9Melhoa2OwvFrEMTJguCHaBkNVHYeP552O7hfQYVWU";
  public static final String TEST_CODE_CHALLENGE_2 = "ysJXbKHz-FWCDD3vYpbFchqeQflbzg2yjdiTJD4EUl8";
  public static final String TEST_CODE_VERIFIER_2 =
      "5UbkjcmBPZ8ufxbmCBR07RXXxtV6Iu-r36LHDLn0hI9JxQBzGTA_xzNVkID6zyHg";
  public static final String TEST_NONCE = "test_nonce_123";

  // Authorization Parameter Names
  public static final String PARAM_STATE = "state";
  public static final String PARAM_CLIENT_ID = "client_id";
  public static final String PARAM_SCOPE = "scope";
  public static final String PARAM_REDIRECT_URI = "redirect_uri";
  public static final String PARAM_RESPONSE_TYPE = "response_type";
  public static final String PARAM_CODE_CHALLENGE = "code_challenge";
  public static final String PARAM_CODE_CHALLENGE_METHOD = "code_challenge_method";
  public static final String PARAM_PROMPT = "prompt";
  public static final String PARAM_LOGIN_HINT = "login_hint";
  public static final String PARAM_NONCE = "nonce";

  // Authorization Headers
  public static final String HEADER_LOCATION = "Location";

  // Authorization URLs
  public static final String LOGIN_PAGE_URL = "https://auth.example.com/login";
  public static final String LOGIN_CHALLENGE = "login_challenge";

  // Authorization Error Types
  public static final String ERROR_INVALID_SCOPE = "invalid_scope";
  public static final String ERROR_UNSUPPORTED_RESPONSE_TYPE = "unsupported_response_type";
  public static final String ERROR_INVALID_CLIENT = "invalid_client";
  public static final String ERROR_INVALID_REDIRECT_URI = "invalid_redirect_uri";
  public static final String ERROR_CLIENT_AUTHENTICATION_FAILED = "Client authentication failed";
  public static final String ERROR_REDIRECT_URI_INVALID = "Redirect uri is invalid";
  public static final String ERROR_SCOPE_MUST_CONTAIN_OPENID = "scope must contain 'openid'";
  public static final String ERROR_RESPONSE_TYPE_REQUIRED = "response_type is required";
  public static final String ERROR_CLIENT_ID_REQUIRED = "client_id is required";
  public static final String ERROR_SCOPE_REQUIRED = "scope is required";
  public static final String ERROR_REDIRECT_URI_REQUIRED = "redirect_uri is required";
  public static final String ERROR_LOGIN_CHALLENGE_REQUIRED = "login_challenge is required";
  public static final String ERROR_REFRESH_TOKEN_REQUIRED = "refresh_token is required";
  public static final String ERROR_UNAUTHORIZED = "unauthorized";
  public static final String ERROR_CODE_CHALLENGE_TOGETHER =
      "code_challenge and code_challenge_method must be provided together";

  // Authorization Test Values
  public static final String AUTH_RESPONSE_TYPE_CODE = "code";
  public static final String AUTH_PROMPT_LOGIN = "login";
  public static final String AUTH_PROMPT_CONSENT = "consent";
  public static final String AUTH_PROMPT_NONE = "none";
  public static final String AUTH_PROMPT_SELECT_ACCOUNT = "select_account";
  public static final String AUTH_PROMPT_INVALID = "invalid_prompt";
  public static final String AUTH_CODE_CHALLENGE_METHOD_INVALID = "invalid_method";
  public static final String AUTH_RESPONSE_TYPE_TOKEN = "token";
  public static final String AUTH_TEST_CLIENT_NAME = "Test OIDC Client";

  // Authorization Code Challenge Methods
  public static final String AUTH_CODE_CHALLENGE_METHOD_S256 = "S256";
  public static final String AUTH_CODE_CHALLENGE_METHOD_PLAIN = "plain";

  // Authorization Test URLs
  public static final String MALICIOUS_CALLBACK_URL = "https://malicious.com/callback";
  public static final String INVALID_CLIENT_ID = "invalid_client_id";
  public static final String INVALID_CLIENT_SECRET = "invalid_client_secret";

  // Authorization Test Boolean Values
  public static final boolean AUTH_SKIP_CONSENT_FALSE = false;

  // Authorization Test Special Values
  public static final String AUTH_STATE_SPECIAL_CHARS = "state_with_special_chars_!@#$%^&*()";

  // Authorization Test Constants
  public static final String PARAM_SEPARATOR = "&";
  public static final String QUERY_SEPARATOR = "\\?";
  public static final String EQUALS_SIGN = "=";

  // Authorization Test Header Formats
  public static final String LOGIN_CHALLENGE_PARAM = LOGIN_CHALLENGE + EQUALS_SIGN;
  public static final String STATE_PARAM_FORMAT = PARAM_STATE + EQUALS_SIGN + "%s";
  public static final String PROMPT_PARAM_FORMAT = PARAM_PROMPT + EQUALS_SIGN + "%s";
  public static final String LOGIN_HINT_PARAM_FORMAT = PARAM_LOGIN_HINT + EQUALS_SIGN + "%s";

  // Authorization Error Parameters
  public static final String PARAM_ERROR = "error";
  public static final String PARAM_ERROR_DESCRIPTION = "error_description";

  // Authorization Error Parameter Formats
  public static final String ERROR_PARAM_FORMAT = PARAM_ERROR + EQUALS_SIGN + "%s";
  public static final String ERROR_DESC_PARAM_FORMAT = PARAM_ERROR_DESCRIPTION + EQUALS_SIGN + "%s";
  public static final String ERROR_DESCRIPTION = "error_description";
  public static final String ERROR_FIELD = "error";

  // Login Accept Test Constants
  public static final String ERROR_INVALID_CHALLENGE = "Invalid challenge";
  public static final String ERROR_INVALID_REFRESH_TOKEN = "Invalid refresh token";
  public static final String TEST_USER_ID = "testuser";
  public static final String TEST_USER_ID_2 = "testuser_2";
  public static final String TEST_USER_ID_3 = "testuser_3";
  public static final String PARTIAL_CONSENT_USER_ID = "partial_consent_user";
  public static final String FULL_CONSENT_USER_ID = "full_consent_user";
  public static final String DETAILED_VALIDATION_USER_ID = "detailed_validation_user";
  public static final String SKIP_CONSENT_CLIENT_NAME = "Skip Consent Client";
  public static final String SOURCE_VALUE = "source";
  public static final String DEVICE_VALUE = "device1";
  public static final String LOCATION_VALUE = "location";
  public static final String IP_ADDRESS = "1.2.3.4";

  // Token Test
  public static final String INVALID_REFRESH_TOKEN = "invalid_refresh_token";
  public static final String TOKEN = "token";

  // Token Endpoint Parameter Names
  public static final String TOKEN_PARAM_GRANT_TYPE = "grant_type";
  public static final String TOKEN_PARAM_SCOPE = "scope";
  public static final String TOKEN_PARAM_CODE = "code";
  public static final String TOKEN_PARAM_REDIRECT_URI = "redirect_uri";
  public static final String TOKEN_PARAM_ID_TOKEN = "id_token";
  public static final String TOKEN_PARAM_CODE_VERIFIER = "code_verifier";
  public static final String TOKEN_PARAM_REFRESH_TOKEN = "refresh_token";
  public static final String TOKEN_PARAM_ACCESS_TOKEN = "access_token";
  public static final String TOKEN_PARAM_TOKEN_TYPE = "token_type";
  public static final String TOKEN_PARAM_EXPIRES_IN = "expires_in";

  // Token Endpoint Error Types
  public static final String TOKEN_ERROR_INVALID_REQUEST = "invalid_request";
  public static final String TOKEN_ERROR_UNSUPPORTED_GRANT_TYPE = "unsupported_grant_type";
  public static final String TOKEN_ERROR_INVALID_CLIENT = "invalid_client";
  public static final String TOKEN_ERROR_UNAUTHORIZED_CLIENT = "unauthorized_client";
  public static final String TOKEN_ERROR_INVALID_SCOPE = "invalid_scope";
  public static final String TOKEN_ERROR_INVALID_GRANT = "invalid_grant";

  // Token Endpoint Error Messages
  public static final String TOKEN_ERROR_MSG_UNSUPPORTED_GRANT_TYPE =
      "The grant type '%s' is not supported";
  public static final String TOKEN_ERROR_MSG_CLIENT_AUTH_FAILED = "Client authentication failed";
  public static final String TOKEN_ERROR_MSG_AUTH =
      "Both 'Authorization' header and 'client_id' parameter are missing";
  public static final String TOKEN_ERROR_MSG_UNAUTHORIZED_CLIENT =
      "The authenticated client is not authorized to use this authorization grant type";
  public static final String TOKEN_ERROR_MSG_INVALID_SCOPE =
      "The requested scope is invalid, unknown, malformed, or exceeds the scope granted by the resource owner";
  public static final String TOKEN_ERROR_MSG_REFRESH_TOKEN_INVALID = "refresh_token is invalid";
  public static final String TOKEN_ERROR_MSG_REFRESH_TOKEN_EXPIRED = "refresh_token is expired";
  public static final String TOKEN_ERROR_MSG_AUTHORIZATION_CODE_INVALID = "code is invalid";
  public static final String TOKEN_ERROR_MSG_REDIRECT_URI_INVALID = "redirect_uri is invalid";
  public static final String TOKEN_ERROR_MSG_CODE_VERIFIER_INVALID = "code_verifier is invalid";
  public static final String TOKEN_ERROR_MSG_CODE_VERIFIER_REQUIRED = "code_verifier is required";
  public static final String TOKEN_ERROR_MSG_CODE_REQUIRED = "code is required";
  public static final String TOKEN_ERROR_MSG_REDIRECT_URI_REQUIRED = "redirect_uri is required";

  // Token Response Values
  public static final String TOKEN_TYPE_BEARER = "Bearer";

  // HTTP Headers for Token Requests
  public static final String HEADER_AUTHORIZATION = "Authorization";
  public static final String HEADER_CONTENT_TYPE = "Content-Type";
  public static final String HEADER_WWW_AUTHENTICATE = "WWW-Authenticate";
  public static final String HEADER_CACHE_CONTROL = "Cache-Control";
  public static final String HEADER_PRAGMA = "Pragma";
  public static final String CACHE_CONTROL_NO_STORE = "no-store";
  public static final String PRAGMA_NO_CACHE = "no-cache";
  public static final String CONTENT_TYPE_FORM_URLENCODED = "application/x-www-form-urlencoded";
  public static final String AUTH_BASIC_PREFIX = "Basic ";
  public static final String WWW_AUTHENTICATE_BASIC_REALM_FORMAT = "Basic realm=\"%s\"";

  // JWT Token Constants
  public static final String JWT_ALGORITHM_RS256 = "RS256";
  public static final String JWT_TYPE_ACCESS_TOKEN = "at+jwt";
  public static final String TEST_KID = "test-kid";
  public static final String TEST_ISSUER = "https://auth.example.com";
  public static final String TEST_PUBLIC_KEY_PATH =
      "src/test/resources/test-data/tenant1-public-key.pem";

  // Token Test Data
  public static final String TEST_DEVICE_NAME = "device1";
  public static final String TEST_IP_ADDRESS = "1.2.3.4";
  public static final long ACCESS_TOKEN_EXPIRY_SECONDS = 900L;
  public static final long ID_TOKEN_EXPIRY_SECONDS = 3600L;
  public static final long REFRESH_TOKEN_EXPIRY_SECONDS = 1800L;
  public static final long EXPIRED_TOKEN_OFFSET_SECONDS = -1800L;
}
