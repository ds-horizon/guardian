package com.dreamsportslabs.guardian.dto.request;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_REQUEST;

import com.dreamsportslabs.guardian.constant.Constants;
import com.dreamsportslabs.guardian.constant.Flow;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class V1AuthFbRequestDto {
  private String accessToken;
  private String responseType;
  private Flow flow;
  private MetaInfo metaInfo;
  @JsonIgnore private Map<String, Object> additionalInfo;

  public V1AuthFbRequestDto() {
    this.flow = Flow.SIGNINUP;
    this.metaInfo = new MetaInfo();
    this.additionalInfo = new HashMap<>();
  }

  @JsonAnyGetter
  public Map<String, Object> getAdditionalInfo() {
    return this.additionalInfo;
  }

  @JsonAnySetter
  public void addAdditionalInfo(String key, Object value) {
    this.additionalInfo.put(key, value);
  }

  public void validate() {
    if (responseType == null) {
      throw INVALID_REQUEST.getCustomException("Invalid response type");
    }

    if (accessToken == null) {
      throw INVALID_REQUEST.getCustomException("Invalid access token");
    }

    if (!Constants.fbAuthResponseTypes.contains(responseType)) {
      throw INVALID_REQUEST.getCustomException("Invalid response type");
    }
  }
}
