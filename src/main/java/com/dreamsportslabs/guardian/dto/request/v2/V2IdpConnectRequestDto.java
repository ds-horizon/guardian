package com.dreamsportslabs.guardian.dto.request.v2;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
public class V2IdpConnectRequestDto extends V2AbstractAuthenticationRequestDto {
  @JsonProperty("id_provider")
  @NotBlank(message = "id_provider is required")
  private String idProvider;

  @JsonProperty("identifier")
  @NotBlank(message = "identifier is required")
  private String identifier;

  @JsonProperty("identifier_type")
  @NotBlank(message = "identifier_type is required")
  private String identifierType;

  @JsonProperty("response_type")
  @NotBlank(message = "response_type is required")
  private String responseType;

  @JsonProperty("nonce")
  private String nonce;

  @JsonProperty("code_verifier")
  private String codeVerifier;
}
