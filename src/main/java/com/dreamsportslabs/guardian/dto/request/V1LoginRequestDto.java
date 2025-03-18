package com.dreamsportslabs.guardian.dto.request;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_REQUEST;

import com.dreamsportslabs.guardian.constant.Constants;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;

@Data
public class V1LoginRequestDto {
  private String username;
  private String password;
  private String responseType;
  private MetaInfo metaInfo;
  @JsonIgnore private Map<String, Object> additionalInfo;

  public V1LoginRequestDto() {
    this.additionalInfo = new HashMap<>();
    this.metaInfo = new MetaInfo();
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

    if (username == null) {
      throw INVALID_REQUEST.getCustomException("Missing username");
    }

    if (password == null) {
      throw INVALID_REQUEST.getCustomException("Missing password");
    }

    if (!Constants.loginResponseTypes.contains(responseType)) {
      throw INVALID_REQUEST.getCustomException("Invalid response type");
    }
  }
}
