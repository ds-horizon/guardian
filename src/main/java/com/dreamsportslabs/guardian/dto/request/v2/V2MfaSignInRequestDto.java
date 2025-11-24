package com.dreamsportslabs.guardian.dto.request.v2;

import com.dreamsportslabs.guardian.constant.MfaFactor;
import com.dreamsportslabs.guardian.validation.annotation.ValidV2MfaSignInRequest;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@ValidV2MfaSignInRequest
public class V2MfaSignInRequestDto {

  @JsonProperty("factor")
  @NotNull(message = "factor cannot be null or empty")
  private MfaFactor factor;

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

  @JsonProperty("refresh_token")
  @NotBlank(message = "refresh_token cannot be null or empty")
  private String refreshToken;

  @JsonProperty("scopes")
  private List<String> scopes = new ArrayList<>();

  @JsonProperty("client_id")
  @NotNull(message = "client_id cannot be null or empty")
  private String clientId;

  public void setScopes(List<String> scopes) {
    if (scopes == null) this.scopes = new ArrayList<>();
    else this.scopes = scopes;
  }
}
