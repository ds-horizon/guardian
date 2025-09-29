package com.dreamsportslabs.guardian.dao.model;

import com.dreamsportslabs.guardian.constant.AuthMethod;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OidcCodeModel {
  private String userId;
  private String clientId;
  private String scope;
  private String redirectUri;
  private String nonce;
  private String codeChallenge;
  private String codeChallengeMethod;
  private List<AuthMethod> authMethods = new ArrayList<>();

  public OidcCodeModel(AuthorizeSessionModel session) {
    this.userId = session.getUserId();
    this.clientId = session.getClient().getClientId();
    this.scope = String.join(" ", session.getConsentedScopes());
    this.redirectUri = session.getRedirectUri();
    this.nonce = session.getNonce();
    this.codeChallenge = session.getCodeChallenge();
    this.codeChallengeMethod =
        session.getCodeChallengeMethod() != null
            ? session.getCodeChallengeMethod().getValue()
            : null;
  }
}
