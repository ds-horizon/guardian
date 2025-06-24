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
  public static final String BODY_PARAM_OTP = "otp";
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

  // Scope Configuration Params
  public static final String BODY_PARAM_SCOPE = "name";
  public static final String BODY_PARAM_DISPLAY_NAME = "displayName";
  public static final String BODY_PARAM_DESCRIPTION = "description";
  public static final String BODY_PARAM_CLAIMS = "claims";
  public static final String BODY_PARAM_ICON_URL = "iconUrl";
  public static final String BODY_PARAM_IS_OIDC = "isOidc";

  // Request Body Params Values
  public static final String BODY_CHANNEL_EMAIL = "EMAIL";
  public static final String BODY_CHANNEL_SMS = "SMS";

  // Test Data Values
  public static final String TEST_SCOPE_NAME = "Test Scope";
  public static final String TEST_DESCRIPTION = "Test description";
  public static final String TEST_ICON_URL = "https://example.com/icon.png";
  public static final String TEST_EMAIL_CLAIM = "email";
  public static final String TEST_NAME_CLAIM = "name";
  public static final String TEST_PICTURE_CLAIM = "picture";
  public static final String TEST_PHONE_CLAIM = "phone";

  // Tenant IDs
  public static final String TENANT_1 = "tenant1";
  public static final String TENANT_2 = "tenant2";

  // Error messages
  public static final String ERROR_MSG_SCOPE_REQUIRED = "scope name is required";
  public static final String ERROR_MSG_OIDC_REQUIRED = "isOidc field is required";
  public static final String ERROR_CODE_SCOPE_ALREADY_EXISTS = "scope_already_exists";
  public static final String ERROR_MSG_SCOPE_ALREADY_EXISTS = "Scope already exists for tenant";

  // Passwordless Flow
  public static final String PASSWORDLESS_FLOW_SIGNINUP = "SIGNINUP";
  public static final String PASSWORDLESS_FLOW_SIGNUP = "SIGNUP";
  public static final String PASSWORDLESS_FLOW_SIGNIN = "SIGNIN";

  // JWT header claims
  public static final String JWT_HEADER_KID = "kid";
  public static final String JWT_HEADER_ALG = "alg";

  // JWT Payload claims
  public static final String JWT_CLAIM_IAT = "iat";
  public static final String JWT_CLAIM_EXP = "exp";
  public static final String JWT_CLAIM_ISS = "iss";
  public static final String JWT_CLAIM_SUB = "sub";
  public static final String JWT_CLAIM_RFT_ID = "rft_id";

  // Error Response
  public static final String ERROR = "error";
  public static final String CODE = "code";
  public static final String MESSAGE = "message";
  public static final String METADATA = "metadata";
  public static final String ERROR_INVALID_REQUEST = "invalid_request";
  public static final String ERROR_USER_NOT_EXISTS = "user_not_exists";
  public static final String ERROR_USER_EXISTS = "user_exists";
  public static final String ERROR_RESENDS_NOT_ALLOWED = "resends_not_allowed";
  public static final String ERROR_INVALID_STATE = "invalid_state";
  public static final String ERROR_INCORRECT_OTP = "incorrect_otp";
  public static final String ERROR_RESENDS_EXHAUSTED = "resends_exhausted";
  public static final String ERROR_RETRIES_EXHAUSTED = "retries_exhausted";
  public static final String ERROR_UNAUTHORIZED = "unauthorized";
  public static final String ERROR_SMS_SERVICE = "sms_service_error";
  public static final String INVALID_STATE = "invalid_state";
  public static final String ERROR_INTERNAL_ERROR = "internal_error";

  // Response Body Params
  public static final String RESPONSE_BODY_PARAM_STATE = "state";
  public static final String RESPONSE_BODY_PARAM_TRIES = "tries";
  public static final String RESPONSE_BODY_PARAM_RETRIES_LEFT = "retriesLeft";
  public static final String RESPONSE_BODY_PARAM_RESENDS = "resends";
  public static final String RESPONSE_BODY_PARAM_RESENDS_LEFT = "resendsLeft";
  public static final String RESPONSE_BODY_PARAM_RESEND_AFTER = "resendAfter";
  public static final String RESPONSE_BODY_PARAM_IS_NEW_USER = "isNewUser";
  public static final String RESPONSE_BODY_PARAM_RETRIES_LEFT_METADATA = "retriesLeft";

  // Passwordless Model
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
}
