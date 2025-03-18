package com.dreamsportslabs.guardian.dto.request;

import static com.dreamsportslabs.guardian.constant.Constants.REVOCATIONS_FLOOR_FACTOR_2;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_REQUEST;

import jakarta.ws.rs.QueryParam;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import lombok.Data;

@Data
public class RevocationRequestDto {
  @QueryParam("from")
  private Long fromEpoch;

  private Long toEpoch;

  public void validate(Integer accessTokenExpiryInSeconds) {
    Instant fromInstant;
    toEpoch = Instant.now().getEpochSecond();
    if (fromEpoch == null) {
      fromInstant = Instant.now().minus(accessTokenExpiryInSeconds, ChronoUnit.SECONDS);
      fromEpoch = fromInstant.getEpochSecond();
      fromEpoch = fromEpoch - fromEpoch % REVOCATIONS_FLOOR_FACTOR_2;
      return;
    }
    try {
      fromInstant = Instant.ofEpochSecond(fromEpoch);
    } catch (Exception e) {
      throw INVALID_REQUEST.getCustomException("Invalid timestamp");
    }
    if (fromInstant.isBefore(Instant.now().minus(accessTokenExpiryInSeconds, ChronoUnit.SECONDS))) {
      fromInstant = Instant.now().minus(accessTokenExpiryInSeconds, ChronoUnit.SECONDS);
      fromEpoch = fromInstant.getEpochSecond();
    } else if (fromInstant.isAfter(Instant.now())) {
      throw INVALID_REQUEST.getCustomException("from Timestamp is not in allowed range");
    }
  }
}
