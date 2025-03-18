package com.dreamsportslabs.guardian.dto.request;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_REQUEST;

import com.dreamsportslabs.guardian.constant.Constants;
import com.dreamsportslabs.guardian.constant.Contact;
import com.dreamsportslabs.guardian.constant.Flow;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@Slf4j
public class V1PasswordlessInitRequestDto {
  private String state;
  private Flow flow;
  private String responseType;
  private List<Contact> contacts;
  private MetaInfo metaInfo;
  @JsonIgnore private Map<String, Object> additionalInfo;

  public V1PasswordlessInitRequestDto() {
    this.flow = Flow.SIGNINUP;
    this.metaInfo = new MetaInfo();
    this.additionalInfo = new HashMap<>();
  }

  @JsonAnyGetter
  public Map<String, Object> getAdditionalInfo() {
    return additionalInfo;
  }

  @JsonAnySetter
  public void addAdditionalInfo(String name, Object value) {
    additionalInfo.put(name, value);
  }

  private boolean isValidState() {
    return state != null;
  }

  private boolean isValidContact() {
    if (contacts.isEmpty()) {
      return false;
    }
    boolean res = true;
    for (Contact contact : contacts) {
      res = contact.validate();
    }
    return res;
  }

  public void validate() {
    if (isValidState()) {
      return;
    }

    if (!isValidContact()) {
      throw INVALID_REQUEST.getCustomException("Invalid contact");
    }

    if (!Constants.passwordlessAuthResponseTypes.contains(this.responseType)) {
      throw INVALID_REQUEST.getCustomException("Invalid response type");
    }
  }
}
