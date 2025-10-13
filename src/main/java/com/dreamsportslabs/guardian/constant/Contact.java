package com.dreamsportslabs.guardian.constant;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Contact {
  @JsonProperty("channel")
  private Channel channel;

  @JsonProperty("identifier")
  private String identifier;

  @JsonProperty("template")
  private Template template;

  public boolean validate() {
    if (identifier == null || channel == null) {
      return false;
    }

    return template == null || template.validate();
  }
}
