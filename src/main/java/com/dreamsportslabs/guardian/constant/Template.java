package com.dreamsportslabs.guardian.constant;

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
  private String name;
  private Map<String, String> params;

  public boolean validate() {
    return !StringUtils.isBlank(this.name);
  }
}
