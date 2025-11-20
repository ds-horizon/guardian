package com.dreamsportslabs.guardian.filter;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@PreMatching
@Provider
public class CorsRequestFilter implements ContainerRequestFilter {

  @Override
  public void filter(ContainerRequestContext requestContext) {
    // Handle preflight OPTIONS request
    if ("OPTIONS".equalsIgnoreCase(requestContext.getMethod())) {
      String origin = requestContext.getHeaderString("Origin");
      if (origin == null || origin.isEmpty()) {
        origin = "*";
      }

      Response.ResponseBuilder responseBuilder =
          Response.ok()
              .header("Access-Control-Allow-Origin", origin)
              .header("Access-Control-Allow-Credentials", "true")
              .header(
                  "Access-Control-Allow-Headers",
                  "Origin, Content-Type, Accept, Authorization, tenant-id, X-Requested-With")
              .header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, PATCH")
              .header("Access-Control-Max-Age", "3600");

      requestContext.abortWith(responseBuilder.build());
    }
  }
}
