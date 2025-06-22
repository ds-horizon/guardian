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
public class ConsentSessionModel {
  private String consentChallenge;
  private String userId;
  private ClientModel client;
  private List<String> requestedScopes;
  private String redirectUri;
  private String state;
  private String nonce;
  private String codeChallenge;
  private String codeChallengeMethod;
  private String loginChallenge;

  @Override
  public String toString() {
    ObjectMapper objectMapper = new ObjectMapper();
    try {
      return objectMapper.writeValueAsString(this);
    } catch (JsonProcessingException e) {
      log.error("Error converting ConsentSessionModel to String: {}", e.getMessage());
      return null;
    }
  }
} 