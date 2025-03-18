package com.dreamsportslabs.guardian.utils;

import static com.dreamsportslabs.guardian.constant.Constants.prohibitedForwardingHeaders;

import io.vertx.rxjava3.core.MultiMap;
import jakarta.ws.rs.core.MultivaluedMap;
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
}
