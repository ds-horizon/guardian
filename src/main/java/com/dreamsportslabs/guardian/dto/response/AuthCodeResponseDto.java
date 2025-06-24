package com.dreamsportslabs.guardian.dto.response;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import jakarta.ws.rs.core.UriBuilder;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthCodeResponseDto {
  private String redirectUri;
  private String state;
  private String code;

  public ResponseBuilder toResponse() {
    UriBuilder uriBuilder = UriBuilder.fromUri(redirectUri);

    if (code != null) {
      uriBuilder.queryParam("code", code);
    }

    if (state != null) {
      uriBuilder.queryParam("state", state);
    }

    return Response.status(Response.Status.FOUND).location(uriBuilder.build());
  }
}
