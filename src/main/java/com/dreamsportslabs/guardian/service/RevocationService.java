package com.dreamsportslabs.guardian.service;

import static com.dreamsportslabs.guardian.constant.Constants.REVOCATIONS_FLOOR_FACTOR;
import static com.dreamsportslabs.guardian.constant.Constants.REVOCATIONS_KEY_SEPARATOR;

import com.dreamsportslabs.guardian.cache.RevocationsCache;
import com.dreamsportslabs.guardian.config.tenant.TenantConfig;
import com.dreamsportslabs.guardian.dto.request.RevocationRequestDto;
import com.dreamsportslabs.guardian.dto.response.RevocationsResponseDto;
import com.dreamsportslabs.guardian.registry.Registry;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class RevocationService {
  private final RevocationsCache revocationsCache;
  private final Registry registry;

  public Single<RevocationsResponseDto> getRevocations(
      RevocationRequestDto requestDto, String tenantId) {
    TenantConfig config = registry.get(tenantId, TenantConfig.class);

    Integer accessTokenExpiry = config.getTokenConfig().getAccessTokenExpiry();
    requestDto.validate(accessTokenExpiry);

    Long fromEpoch = getFloorTimestamp(requestDto.getFromEpoch());
    Long toEpoch = getFloorTimestamp(requestDto.getToEpoch());
    if (fromEpoch.equals(toEpoch)) {
      return Single.just(
          new RevocationsResponseDto(new ArrayList<>(), fromEpoch, toEpoch, accessTokenExpiry));
    }
    return revocationsCache
        .getRevocationList(getRevocationKey(tenantId, fromEpoch, toEpoch))
        .map(
            revocations ->
                new RevocationsResponseDto(revocations, fromEpoch, toEpoch, accessTokenExpiry));
  }

  private String getRevocationKey(String tenantId, Long fromTimestamp, Long toTimestamp) {
    return tenantId
        + REVOCATIONS_KEY_SEPARATOR
        + fromTimestamp
        + REVOCATIONS_KEY_SEPARATOR
        + toTimestamp;
  }

  private Long getFloorTimestamp(Long epochSeconds) {
    return epochSeconds - epochSeconds % REVOCATIONS_FLOOR_FACTOR;
  }
}
