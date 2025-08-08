package com.dreamsportslabs.guardian.dto.request;

import static com.dreamsportslabs.guardian.constant.Constants.FLOW_SIGNINUP;
import static com.dreamsportslabs.guardian.constant.Constants.IDENTIFIER_TYPE_CODE;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_REQUEST;

import com.dreamsportslabs.guardian.constant.Flow;
import com.dreamsportslabs.guardian.constant.IdentifierType;
import com.dreamsportslabs.guardian.constant.ResponseType;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Setter
@Getter
public class IdpConnectRequestDto {
  private String idProvider;
  private String identifier;
  private String identifierType;
  private String responseType;
  private String nonce;
  private String codeVerifier;
  private String flow;
  private MetaInfo metaInfo;
  @JsonIgnore private Map<String, Object> additionalInfo;
  @JsonIgnore private ResponseType idpResponseType;
  @JsonIgnore private Flow loginFlow;
  @JsonIgnore private IdentifierType userIdentifierType;

  @JsonAnyGetter
  public Map<String, Object> getAdditionalInfo() {
    return this.additionalInfo;
  }

  @JsonAnySetter
  public void addAdditionalInfo(String key, Object value) {
    this.additionalInfo.put(key, value);
  }

  public IdpConnectRequestDto() {
    this.loginFlow = Flow.SIGNINUP;
    this.metaInfo = new MetaInfo();
    this.additionalInfo = new HashMap<>();
    this.userIdentifierType = IdentifierType.CODE;
  }

  public void validate() {
    if (StringUtils.isBlank(idProvider)) {
      throw INVALID_REQUEST.getCustomException("idProvider is required");
    }

    if (StringUtils.isBlank(identifier)) {
      throw INVALID_REQUEST.getCustomException("identifier is required");
    }

    if (StringUtils.isBlank(responseType)) {
      throw INVALID_REQUEST.getCustomException("response type is required");
    }

    setIdpResponseType();
    setLoginFlow();
    setUserIdentifierType();
  }

  private void setIdpResponseType() {
    try {
      this.idpResponseType = ResponseType.valueOf(responseType.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw INVALID_REQUEST.getCustomException("Unsupported response_type: '" + responseType + "'");
    }
  }

  private void setLoginFlow() {
    try {
      String flowValue = StringUtils.isBlank(flow) ? FLOW_SIGNINUP : flow.toUpperCase();
      this.loginFlow = Flow.valueOf(flowValue);
    } catch (IllegalArgumentException e) {
      throw INVALID_REQUEST.getCustomException("Unsupported flow: '" + flow + "'");
    }
  }

  private void setUserIdentifierType() {
    try {
      String userIdentifierTypeValue =
          StringUtils.isBlank(identifierType) ? IDENTIFIER_TYPE_CODE : identifierType.toUpperCase();
      this.userIdentifierType = IdentifierType.valueOf(userIdentifierTypeValue);
    } catch (IllegalArgumentException e) {
      throw INVALID_REQUEST.getCustomException(
          "Unsupported identifierType: '" + identifierType + "'");
    }
  }
}
