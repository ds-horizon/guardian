package com.dreamsportslabs.guardian.dto;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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
  private String clientId;

  @JsonIgnore private static final Set<String> knownFields = new HashSet<>();

  @JsonIgnore @Builder.Default private Map<String, Object> additionalInfo = new HashMap<>();

  @JsonAnyGetter
  public Map<String, Object> getAdditionalInfo() {
    return additionalInfo;
  }

  static {
    for (Field field : UserDto.class.getDeclaredFields()) {
      knownFields.add(field.getName());
    }
  }

  @JsonAnySetter
  public void addAdditionalInfo(String key, Object value) {
    if (!knownFields.contains(key)) {
      additionalInfo.put(key, value);
    }
  }

  public void setAdditionalInfo(Map<String, Object> map) {
    if (map == null) {
      return;
    }
    map.forEach(this::addAdditionalInfo);
  }
}
