package com.dreamsportslabs.guardian.constant;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_REQUEST;

import java.util.Arrays;
import java.util.List;
import lombok.Getter;

@Getter
public enum BlockFlow {
  PASSWORDLESS("passwordless", List.of("/v1/passwordless/init", "/v1/passwordless/complete")),
  PASSWORD("password", List.of("/v1/signin", "/v1/signup")),
  SOCIAL_AUTH(
      "social_auth",
      List.of(
          "/v1/auth/fb",
          "/v1/auth/google",
          "/v1/idp/connect",
          "/v2/auth/fb",
          "/v2/auth/google",
          "/v2/idp/connect")),
  OTP_VERIFY("otp_verify", List.of("/v1/otp/send", "/v1/otp/verify"));

  private final String flowName;
  private final List<String> apiPaths;

  BlockFlow(String flowName, List<String> apiPaths) {
    this.flowName = flowName;
    this.apiPaths = apiPaths;
  }

  public static BlockFlow fromFlowName(String flowName) {
    for (BlockFlow flow : values()) {
      if (flow.getFlowName().equals(flowName)) {
        return flow;
      }
    }
    throw INVALID_REQUEST.getCustomException(
        "Invalid flow: " + flowName + ". Valid flows are: " + BlockFlow.getAllFlowNames());
  }

  public static List<String> getAllFlowNames() {
    return Arrays.stream(values()).map(BlockFlow::getFlowName).toList();
  }
}
