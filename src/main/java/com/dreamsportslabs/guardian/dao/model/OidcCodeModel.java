package com.dreamsportslabs.guardian.dao.model;

import com.dreamsportslabs.guardian.constant.OidcCodeChallengeMethod;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OidcCodeModel {
  private String userId;
  private ClientModel client;
  private List<String> consentedScopes;
  private String redirectUri;
  private String nonce;
  private String codeChallenge;
  private OidcCodeChallengeMethod codeChallengeMethod;

  public OidcCodeModel(AuthorizeSessionModel session) {
    this.userId = session.getUserId();
    this.client = session.getClient();
    this.consentedScopes = session.getConsentedScopes();
    this.redirectUri = session.getRedirectUri();
    this.nonce = session.getNonce();
    this.codeChallenge = session.getCodeChallenge();
    this.codeChallengeMethod = session.getCodeChallengeMethod();
  }
}
