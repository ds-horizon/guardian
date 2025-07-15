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

  // Request Body Params Values

  public static final String BODY_CHANNEL_EMAIL = "EMAIL";
  public static final String BODY_CHANNEL_SMS = "SMS";

  public static final String TEST_SCOPE_NAME = "Test Scope";
  public static final String TEST_DESCRIPTION = "Test description";
  public static final String TEST_DISPLAY_NAME = "Test display name";
  public static final String TEST_ICON_URL = "https://example.com/icon.png";
  public static final String TEST_EMAIL_CLAIM = "email";
  public static final String TEST_NAME_CLAIM = "name";
  public static final String TEST_PICTURE_CLAIM = "picture";
  public static final String TEST_PHONE_CLAIM = "phone";

  // Predefined scope names
  public static final String SCOPE_OPENID = "openid";
  public static final String SCOPE_PHONE = "phone";
  public static final String SCOPE_EMAIL = "email";
  public static final String SCOPE_ADDRESS = "address";

  // Predefined claim names
  public static final String CLAIM_SUB = "sub";
  public static final String CLAIM_PHONE_NUMBER = "phone_number";
  public static final String CLAIM_PHONE_NUMBER_VERIFIED = "phone_number_verified";
  public static final String CLAIM_EMAIL_VERIFIED = "email_verified";
  public static final String CLAIM_ADDRESS = "address";

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
      "phone scope must include 'phone_number' and 'phone_number_verified' claim";
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
  public static final String JWT_CLAIM_EXP = "exp";
  public static final String JWT_CLAIM_ISS = "iss";
  public static final String JWT_CLAIM_SUB = "sub";
  public static final String JWT_CLAIM_RFT_ID = "rft_id";
  public static final String JWT_CLAIM_TENANT_ID = "tid";

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
}
