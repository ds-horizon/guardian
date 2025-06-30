package com.dreamsportslabs.guardian.dto.request;

import static com.dreamsportslabs.guardian.constant.Constants.FORMAT_JWKS;
import static com.dreamsportslabs.guardian.constant.Constants.FORMAT_PEM;
import static com.dreamsportslabs.guardian.constant.Constants.VALID_KEY_SIZES;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_REQUEST;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class GenerateRsaKeyRequestDto {

  private Integer keySize = 2048;
  private String format = FORMAT_PEM;

  public void validate() {
    // Validate key size
    if (keySize == null) {
      keySize = 2048;
    } else if (!VALID_KEY_SIZES.contains(keySize)) {
      throw INVALID_REQUEST.getCustomException(
          "Invalid RSA key length. Allowed values are [2048, 3072, 4096]");
    }

    if (!StringUtils.equals(this.format, FORMAT_PEM)
        && !StringUtils.equals(this.format, FORMAT_JWKS)) {
      throw INVALID_REQUEST.getCustomException(
          "Invalid key format. Allowed values are PEM or JWKS");
    }
  }
}
