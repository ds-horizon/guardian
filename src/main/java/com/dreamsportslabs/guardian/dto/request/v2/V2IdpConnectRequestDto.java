package com.dreamsportslabs.guardian.dto.request.v2;

import com.dreamsportslabs.guardian.validation.annotation.ValidV2IdpConnectRequest;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@ValidV2IdpConnectRequest
public class V2IdpConnectRequestDto extends V2AbstractAuthenticationRequestDto {
  @JsonProperty("id_provider")
  private String idProvider;

  @JsonProperty("identifier")
  private String identifier;

  @JsonProperty("identifier_type")
  private String identifierType;

  @JsonProperty("response_type")
  private String responseType;

  @JsonProperty("nonce")
  private String nonce;

  @JsonProperty("code_verifier")
  private String codeVerifier;
}
