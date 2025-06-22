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
  private String code;
  private String userId;
  private ClientModel client;
  private List<String> consentedScopes;
  private String redirectUri;
  private String state;
  private String nonce;
  private String codeChallenge;
  private String codeChallengeMethod;
  private Long issuedAt;
  private Long expiresAt;

  public CodeSessionModel(String code, String userId, ClientModel client, List<String> consentedScopes,
                         String redirectUri, String state, String nonce, String codeChallenge, 
                         String codeChallengeMethod) {
    this.code = code;
    this.userId = userId;
    this.client = client;
    this.consentedScopes = consentedScopes;
    this.redirectUri = redirectUri;
    this.state = state;
    this.nonce = nonce;
    this.codeChallenge = codeChallenge;
    this.codeChallengeMethod = codeChallengeMethod;
    this.issuedAt = System.currentTimeMillis();
    this.expiresAt = this.issuedAt + (10 * 60 * 1000);
  }

  public boolean isExpired() {
    return System.currentTimeMillis() > this.expiresAt;
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