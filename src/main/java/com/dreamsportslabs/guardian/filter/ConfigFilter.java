package com.dreamsportslabs.guardian.filter;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.UNAUTHORIZED;

import com.dreamsportslabs.guardian.cache.TenantCache;
import com.dreamsportslabs.guardian.constant.Constants;
import com.dreamsportslabs.guardian.injection.GuiceInjector;
import com.dreamsportslabs.guardian.registry.Registry;
import jakarta.annotation.Priority;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jboss.resteasy.core.interception.jaxrs.SuspendableContainerRequestContext;

@PreMatching
@Priority(1)
@Slf4j
@Provider
public class ConfigFilter implements ContainerRequestFilter {
  final Registry registry;
  final TenantCache tenantCache;

  public ConfigFilter() {
    registry = GuiceInjector.getGuiceInjector().getInstance(Registry.class);
    tenantCache = GuiceInjector.getGuiceInjector().getInstance(TenantCache.class);
  }

  @Override
  public void filter(ContainerRequestContext requestContext) {
    // Todo: create a filter annotation instead of filtering out routes here, @Config
    String path = requestContext.getUriInfo().getPath();
    if (path.equalsIgnoreCase("/healthcheck")) {
      return;
    }

    String tenantId = requestContext.getHeaderString(Constants.TENANT_ID);

    if (StringUtils.isBlank(tenantId)) {
      throw UNAUTHORIZED.getException();
    }

    SuspendableContainerRequestContext suspendableContext =
        (SuspendableContainerRequestContext) requestContext;
    suspendableContext.suspend();

    tenantCache
        .getTenantConfig(tenantId)
        .subscribe(
            r -> suspendableContext.resume(),
            err -> {
              log.error("Error Initializing tenant details", err);
              suspendableContext.resume(err);
            });
  }
}
