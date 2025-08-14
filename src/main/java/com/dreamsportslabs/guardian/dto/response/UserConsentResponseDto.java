package com.dreamsportslabs.guardian.dto.response;

import com.dreamsportslabs.guardian.dao.model.ClientModel;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public class UserConsentResponseDto {

  @JsonProperty("client")
  private ClientModel client;

  @JsonProperty("requested_scopes")
  private List<String> requestedScopes;

  @JsonProperty("consented_scopes")
  private List<String> consentedScopes;

  @JsonProperty("subject")
  private String subject;
}
