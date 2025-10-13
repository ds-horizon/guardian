package com.dreamsportslabs.guardian.dto.request.v2;

import com.dreamsportslabs.guardian.constant.Flow;
import com.dreamsportslabs.guardian.dto.request.MetaInfo;
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
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@Slf4j
public class V2AbstractAuthenticationRequestDto {

  @JsonProperty("client_id")
  @NotNull(message = "client_id cannot be null or empty")
  private String clientId;

  @JsonProperty("scopes")
  private List<String> scopes;

  @JsonProperty("meta_info")
  private MetaInfo metaInfo;

  @JsonProperty("flow")
  private Flow flow;

  @JsonIgnore private Map<String, Object> additionalInfo;

  @JsonAnyGetter
  public Map<String, Object> getAdditionalInfo() {
    return additionalInfo;
  }

  @JsonAnySetter
  public void addAdditionalInfo(String name, Object value) {
    additionalInfo.put(name, value);
  }

  public V2AbstractAuthenticationRequestDto() {
    this.additionalInfo = new HashMap<>();
    this.scopes = new ArrayList<>();
    this.metaInfo = new MetaInfo();
    this.flow = Flow.SIGNINUP;
  }
}
