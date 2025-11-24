package com.dreamsportslabs.guardian.constant;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Template {
  @JsonProperty("name")
  private String name;

  @JsonProperty("params")
  private Map<String, String> params = new HashMap<>();

  public boolean validate() {
    return !StringUtils.isBlank(this.name);
  }
}
