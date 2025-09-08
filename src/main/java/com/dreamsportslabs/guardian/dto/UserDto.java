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
    if (!knownFields.contains(key)) {
      additionalInfo.put(key, value);
    }
  }

  public static class UserDtoBuilder {
    public UserDtoBuilder additionalInfo(Map<String, Object> additionalInfo) {
      if (additionalInfo == null) {
        return this;
      }

      additionalInfo.forEach(
          (key, value) -> {
            if (!knownFields.contains(key)) {
              this.additionalInfo$value.put(key, value);
            }
          });
      return this;
    }
  }
}
