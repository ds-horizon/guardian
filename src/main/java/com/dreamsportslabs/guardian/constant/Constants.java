package com.dreamsportslabs.guardian.constant;

import com.google.common.collect.ImmutableList;
import io.vertx.core.json.JsonObject;
import java.util.ArrayList;

public final class Constants {
  public static final String TENANT_ID = "tenant-id";
  public static final String USERID = "userId";

  public static final String TOKEN = "token";
  public static final String CODE = "code";

  public static final String FACEBOOK = "facebook";
  public static final String GOOGLE = "google";

  public static final String EXPIRY_OPTION_REDIS = "EX";
  public static final String STATIC_OTP_NUMBER = "9";

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

  // JWT CLAIMS
  public static final String JWT_CLAIMS_ISS = "iss";
  public static final String JWT_CLAIMS_SUB = "sub";
  public static final String JWT_CLAIMS_IAT = "iat";
  public static final String JWT_CLAIMS_EXP = "exp";
  public static final String JWT_CLAIMS_RFT_ID = "rft_id";

  public static final ImmutableList<String> fbAuthResponseTypes = ImmutableList.of(CODE, TOKEN);
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

  public static final String EMAIL = "email";
  public static final String PHONE = "phoneNumber";

  public static final String CACHE_KEY_CODE = "CODE";
  public static final String CACHE_KEY_STATE = "STATE";

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
}
