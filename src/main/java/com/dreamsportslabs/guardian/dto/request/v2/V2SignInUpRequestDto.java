package com.dreamsportslabs.guardian.dto.request.v2;

import com.dreamsportslabs.guardian.constant.ResponseType;
import com.dreamsportslabs.guardian.dto.request.MetaInfo;
import com.dreamsportslabs.guardian.validation.annotation.ValidV2SignInUpRequest;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@ValidV2SignInUpRequest
public class V2SignInUpRequestDto {

  @JsonProperty("username")
  private String username;

  @JsonProperty("email")
  private String email;

  @JsonProperty("phone_number")
  private String phoneNumber;

  @JsonProperty("password")
  private String password;

  @JsonProperty("pin")
  private String pin;

  @JsonProperty("response_type")
  @NotNull(message = "response_type cannot be null or empty")
  private ResponseType responseType;

  @JsonProperty("client_id")
  @NotNull(message = "client_id cannot be null or empty")
  private String clientId;

  @JsonProperty("scopes")
  private List<String> scopes = new ArrayList<>();

  @JsonProperty("meta_info")
  private MetaInfo metaInfo = new MetaInfo();

  @JsonIgnore private Map<String, Object> additionalInfo = new HashMap<>();

  @JsonAnyGetter
  public Map<String, Object> getAdditionalInfo() {
    return additionalInfo;
  }

  @JsonAnySetter
  public void addAdditionalInfo(String name, Object value) {
    additionalInfo.put(name, value);
  }

  public void setMetaInfo(MetaInfo metaInfo) {
    if (metaInfo == null) this.metaInfo = new MetaInfo();
    else this.metaInfo = metaInfo;
  }
}
