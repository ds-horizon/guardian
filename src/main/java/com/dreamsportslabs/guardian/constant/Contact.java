package com.dreamsportslabs.guardian.constant;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Contact {
  private Channel channel;
  private String identifier;
  private Template template;

  public boolean validate() {
    if (identifier == null || channel == null) {
      return false;
    }

    return template == null || template.validate();
  }
}
