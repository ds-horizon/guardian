package com.dreamsportslabs.guardian.dao.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Data
@NoArgsConstructor
public class CodeSessionModel {
  private String userId;
  private ClientModel client;
  private List<String> consentedScopes;
  private String redirectUri;
  private String state;
  private String nonce;
  private String codeChallenge;
  private String codeChallengeMethod;

  public CodeSessionModel(AuthorizeSessionModel session) {
    this.userId = session.getUserId();
    this.client = session.getClient();
    this.consentedScopes = session.getConsentedScopes();
    this.redirectUri = session.getRedirectUri();
    this.state = session.getState();
    this.nonce = session.getNonce();
    this.codeChallenge = session.getCodeChallenge();
    this.codeChallengeMethod = session.getCodeChallengeMethod();
  }

  @Override
  public String toString() {
    ObjectMapper objectMapper = new ObjectMapper();
    try {
      return objectMapper.writeValueAsString(this);
    } catch (JsonProcessingException e) {
      log.error("Error converting CodeSessionModel to String: {}", e.getMessage());
      return null;
    }
  }
} 