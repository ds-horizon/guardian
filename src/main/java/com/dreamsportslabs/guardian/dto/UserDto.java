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

  @JsonIgnore private static final Set<String> knownFields = new HashSet<>();
  @JsonIgnore @Builder.Default private Map<String, Object> additionalInfo = new HashMap<>();

  static {
    for (Field field : UserDto.class.getDeclaredFields()) {
      knownFields.add(field.getName());
    }
  }

  @JsonAnyGetter
  public Map<String, Object> getAdditionalInfo() {
    return additionalInfo;
  }

  @JsonAnySetter
  public void addAdditionalInfo(String key, Object value) {
    if (getFieldValue(key) == null || !knownFields.contains(key)) {
      additionalInfo.put(key, value);
    }
  }

  public void setAdditionalInfo(Map<String, Object> map) {
    if (map == null) {
      return;
    }
    map.forEach(this::addAdditionalInfo);
  }

  public Object getFieldValue(String fieldName) {
    try {
      Field field = this.getClass().getDeclaredField(fieldName);
      field.setAccessible(true);
      return field.get(this);
    } catch (Exception e) {
      return null;
    }
  }
}
