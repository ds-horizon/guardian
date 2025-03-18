package com.dreamsportslabs.guardian.dto;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@AllArgsConstructor
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDto {
  private String username;
  private String password;
  private String name;
  private String firstName;
  private String middleName;
  private String lastName;
  private String picture;
  private String email;
  private String phoneNumber;
  private Provider provider;

  @JsonIgnore @Builder.Default private Map<String, Object> additionalInfo = new HashMap<>();

  @JsonAnyGetter
  public Map<String, Object> getAdditionalInfo() {
    return additionalInfo;
  }

  @JsonAnySetter
  public void addAdditionalInfo(String name, Object value) {
    additionalInfo.put(name, value);
  }
}
