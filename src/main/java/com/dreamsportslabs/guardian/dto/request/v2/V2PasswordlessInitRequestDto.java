package com.dreamsportslabs.guardian.dto.request.v2;

import com.dreamsportslabs.guardian.constant.Contact;
import com.dreamsportslabs.guardian.validation.annotation.ValidV2PasswordlessInitRequest;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@Slf4j
@NoArgsConstructor
@ValidV2PasswordlessInitRequest
public class V2PasswordlessInitRequestDto extends V2AbstractAuthenticationRequestDto {
  @JsonProperty("state")
  private String state;

  @JsonProperty("response_type")
  private String responseType;

  @JsonProperty("contacts")
  private List<Contact> contacts;
}
