package com.dreamsportslabs.guardian.dto.request;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_REQUEST;

import com.dreamsportslabs.guardian.constant.Flow;
import com.dreamsportslabs.guardian.constant.ResponseType;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class V1AuthGoogleRequestDto {
  private String idToken;
  private ResponseType responseType;
  private Flow flow;
  private MetaInfo metaInfo;
  @JsonIgnore private Map<String, Object> additionalInfo;

  public V1AuthGoogleRequestDto() {
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
    if (this.responseType == null) {
      throw INVALID_REQUEST.getCustomException("Invalid response type");
    }

    if (idToken == null) {
      throw INVALID_REQUEST.getCustomException("Invalid id token");
    }
  }
}
