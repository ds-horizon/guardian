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

  // JWT header claims
  public static final String JWT_HEADER_KID = "kid";
  public static final String JWT_HEADER_ALG = "alg";

  // JWT Payload claims
  public static final String JWT_CLAIM_IAT = "iat";
  public static final String JWT_CLAIM_EXP = "exp";
  public static final String JWT_CLAIM_ISS = "iss";
  public static final String JWT_CLAIM_SUB = "sub";
  public static final String JWT_CLAIM_RFT_ID = "rft_id";
}
