package com.dreamsportslabs.guardian.constant;

import java.util.Arrays;
import java.util.List;
import lombok.Getter;

@Getter
public enum BlockFlow {
  PASSWORDLESS("passwordless", Arrays.asList("/v1/passwordless/init", "/v1/passwordless/complete")),
  PASSWORD("password", Arrays.asList("/v1/signin", "/v1/signup")),
  SOCIAL_AUTH("social_auth", Arrays.asList("/v1/auth/fb", "/v1/auth/google")),
  OTP_VERIFY("otp_verify", Arrays.asList("/v1/passwordless/complete"));

  private final String flowName;
  private final List<String> apiPaths;

  BlockFlow(String flowName, List<String> apiPaths) {
    this.flowName = flowName;
    this.apiPaths = apiPaths;
  }

  public static BlockFlow fromString(String flowName) {
    for (BlockFlow flow : values()) {
      if (flow.getFlowName().equalsIgnoreCase(flowName)) {
        return flow;
      }
    }
    throw new IllegalArgumentException("Unknown flow: " + flowName);
  }

  public static List<String> getAllFlowNames() {
    return Arrays.stream(values()).map(BlockFlow::getFlowName).toList();
  }
}
