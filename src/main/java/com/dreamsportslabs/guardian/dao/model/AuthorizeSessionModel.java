package com.dreamsportslabs.guardian.dao.model;

import com.dreamsportslabs.guardian.dto.request.AuthorizeRequestDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@NoArgsConstructor
public class AuthorizeSessionModel {
  private String responseType;
  private String scope;
  private ClientModel client;
  private String redirectUri;
  private String state;
  private String nonce;
  private String codeChallenge;
  private String codeChallengeMethod;
  private String prompt;
  private String loginHint;
  private String loginChallenge;
  private String userId;

  public AuthorizeSessionModel(AuthorizeRequestDto requestDto, String loginChallenge) {
    this.responseType = requestDto.getResponseType();
    this.scope = requestDto.getScope();
    this.redirectUri = requestDto.getRedirectUri();
    this.state = requestDto.getState();
    this.nonce = requestDto.getNonce();
    this.codeChallenge = requestDto.getCodeChallenge();
    this.codeChallengeMethod = requestDto.getCodeChallengeMethod();
    this.prompt = requestDto.getPrompt();
    this.loginHint = requestDto.getLoginHint();
    this.loginChallenge = loginChallenge;
  }

  public void setClient(ClientModel client) {
    this.client = client;
  }

  @Override
  public String toString() {
    ObjectMapper objectMapper = new ObjectMapper();
    try {
      return objectMapper.writeValueAsString(this);
    } catch (JsonProcessingException e) {
      log.error("Error converting AuthorizeSessionModel to String: {}", e.getMessage());
      return null;
    }
  }
}
