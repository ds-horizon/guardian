package com.dreamsportslabs.guardian.utils;

import static com.dreamsportslabs.guardian.constant.Constants.prohibitedForwardingHeaders;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.UNAUTHORIZED;

import io.vertx.rxjava3.core.MultiMap;
import jakarta.ws.rs.core.MultivaluedMap;
import java.util.Base64;
import java.util.regex.Pattern;
import org.apache.commons.codec.digest.DigestUtils;

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

  public static String generateBasicAuthHeader(String clientId, String clientSecret) {
    return "Basic "
        + new String(Base64.getEncoder().encode((clientId + ":" + clientSecret).getBytes()));
  }

  public static String[] extractCredentialsFromAuthHeader(String authorizationHeader) {
    try {
      if (!authorizationHeader.startsWith("Basic ")) {
        throw UNAUTHORIZED.getCustomException("Invalid authorization header format");
      }

      String encodedCredentials = authorizationHeader.substring(6).trim();
      String decodedCredentials = new String(Base64.getDecoder().decode(encodedCredentials));
      return decodedCredentials.split(":", 2);
    } catch (Exception e) {
      throw UNAUTHORIZED.getCustomException("Invalid authorization header format");
    }
  }
}
