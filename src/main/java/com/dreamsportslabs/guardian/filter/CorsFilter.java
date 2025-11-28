package com.dreamsportslabs.guardian.filter;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Provider
public class CorsFilter implements ContainerResponseFilter {

  @Override
  public void filter(
      ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
    MultivaluedMap<String, Object> headers = responseContext.getHeaders();

    // Get the origin from the request
    String origin = requestContext.getHeaderString("Origin");
    if (origin == null || origin.isEmpty()) {
      origin = "*"; // Allow all origins if no origin header
    }

    // Add CORS headers
    headers.add("Access-Control-Allow-Origin", origin);
    headers.add("Access-Control-Allow-Credentials", "true");
    headers.add(
        "Access-Control-Allow-Headers",
        "Origin, Content-Type, Accept, Authorization, tenant-id, X-Requested-With");
    headers.add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, PATCH");
    headers.add("Access-Control-Max-Age", "3600");

    // Handle preflight OPTIONS request
    if ("OPTIONS".equalsIgnoreCase(requestContext.getMethod())) {
      responseContext.setStatus(200);
    }
  }
}
