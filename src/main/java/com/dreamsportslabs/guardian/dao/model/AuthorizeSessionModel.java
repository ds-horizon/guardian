package com.dreamsportslabs.guardian.dao.model;

import com.dreamsportslabs.guardian.constant.AuthMethod;
import com.dreamsportslabs.guardian.constant.OidcCodeChallengeMethod;
import com.dreamsportslabs.guardian.constant.OidcPrompt;
import com.dreamsportslabs.guardian.constant.OidcResponseType;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;

@Builder
@Getter
@Jacksonized
@Setter
public class AuthorizeSessionModel {
  private OidcResponseType responseType;
  private List<String> allowedScopes;
  private List<String> consentedScopes;
  private ClientModel client;
  private String redirectUri;
  private String state;
  private String nonce;
  private String codeChallenge;
  private OidcCodeChallengeMethod codeChallengeMethod;
  private OidcPrompt prompt;
  private String loginHint;
  private String userId;
  private List<AuthMethod> authMethods;
}
