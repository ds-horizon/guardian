package com.dreamsportslabs.guardian.dto.request.v1;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_REQUEST;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@Slf4j
public class V1PasswordlessCompleteRequestDto {
  @NotBlank(message = "Invalid State")
  private String state;

  @NotBlank(message = "Invalid OTP")
  private String otp;

  @JsonIgnore private Map<String, Object> additionalInfo;

  public void validate() {
    if (state == null) {
      throw INVALID_REQUEST.getCustomException("Invalid state");
    }
  }

  public V1PasswordlessCompleteRequestDto() {
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
}
