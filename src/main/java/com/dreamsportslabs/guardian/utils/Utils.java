package com.dreamsportslabs.guardian.utils;

import static com.dreamsportslabs.guardian.constant.Constants.USER_AGENT;
import static com.dreamsportslabs.guardian.constant.Constants.X_FORWARDED_FOR;
import static com.dreamsportslabs.guardian.constant.Constants.prohibitedForwardingHeaders;

import com.dreamsportslabs.guardian.exception.ErrorEnum;
import io.vertx.rxjava3.core.MultiMap;
import jakarta.ws.rs.core.MultivaluedMap;
import java.util.Base64;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

public final class Utils {

  private Utils() {
    throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
  }

  // TODO: revalidate the regex
  public static boolean validateEmail(String email) {
    return Pattern.compile("^(.+)@(\\S+)$").matcher(email).matches();
  }

  // TODO: revalidate the regex
  public static boolean validateMobileNumber(String mobile) {
    // Should have 7 to 15 numbers and may or may not include internation code with + sign
    return Pattern.compile("^\\+?[0-9]{7,15}$").matcher(mobile).matches();
  }

  public static MultiMap getForwardingHeaders(MultivaluedMap<String, String> headers) {
    MultiMap forwardingHeaders = MultiMap.caseInsensitiveMultiMap();
    for (String key : headers.keySet()) {
      if (!prohibitedForwardingHeaders.contains(key.toUpperCase())) {
        forwardingHeaders.add(key, headers.getFirst(key));
      }
    }
    return forwardingHeaders;
  }

  public static String getMd5Hash(String input) {
    return DigestUtils.md5Hex(input).toUpperCase();
  }

  public static String[] getCredentialsFromAuthHeader(String authorizationHeader) {
    try {
      String prefix = authorizationHeader.substring(0, 6);
      String token = authorizationHeader.substring(6).strip();
      if (!prefix.equals("Basic ")) {
        throw ErrorEnum.UNAUTHORIZED.getException();
      }
      String credentials;
      credentials = new String(Base64.getDecoder().decode(token.getBytes()));
      return credentials.split(":", 2);
    } catch (Exception e) {
      throw ErrorEnum.UNAUTHORIZED.getException();
    }
  }

  public static String generateBasicAuthHeader(String clientId, String clientSecret) {
    return "Basic "
        + new String(Base64.getEncoder().encode((clientId + ":" + clientSecret).getBytes()));
  }

  public static String getRftId(String refreshToken) {
    if (refreshToken == null) {
      return null;
    }
    return getMd5Hash(refreshToken);
  }

  public static String getIpFromHeaders(MultivaluedMap<String, String> headers) {
    String xForwardedFor = headers.getFirst(X_FORWARDED_FOR);
    if (!StringUtils.isBlank(xForwardedFor)) {
      String[] ips = xForwardedFor.split(",");
      if (ips.length > 0) {
        return ips[0].trim();
      }
    }
    return null;
  }

  public static String getDeviceNameFromHeaders(MultivaluedMap<String, String> headers) {
    String userAgent = headers.getFirst(USER_AGENT);
    if (StringUtils.isBlank(userAgent)) {
      return null;
    }
    return userAgent;
  }

  public static long getCurrentTimeInSeconds() {
    return TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
  }
}
