package com.dreamsportslabs.guardian.service;

import static com.dreamsportslabs.guardian.constant.Constants.FORMAT_PEM;

import com.dreamsportslabs.guardian.dto.request.GenerateRsaKeyRequestDto;
import com.dreamsportslabs.guardian.dto.response.RsaKeyResponseDto;
import com.google.inject.Inject;
import io.fusionauth.jwks.domain.JSONWebKey;
import io.fusionauth.jwt.JWTUtils;
import io.fusionauth.jwt.domain.KeyPair;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class RsaKeyPairGeneratorService {

  public RsaKeyResponseDto generateKey(GenerateRsaKeyRequestDto request) {
    request.validate();
    KeyPair keyPair = getRsaKeyPair(request.getKeySize());

    JSONWebKey publicJsonWebKey = JSONWebKey.build(keyPair.publicKey);
    JSONWebKey privateJsonWebKey = JSONWebKey.build(keyPair.privateKey);

    String kid = JWTUtils.generateJWS_kid(privateJsonWebKey);

    if (request.getFormat().equalsIgnoreCase(FORMAT_PEM)) {
      return RsaKeyResponseDto.builder()
          .kid(kid)
          .privateKey(keyPair.privateKey)
          .publicKey(keyPair.publicKey)
          .keySize(request.getKeySize())
          .build();
    } else {
      return RsaKeyResponseDto.builder()
          .kid(kid)
          .privateKey(privateJsonWebKey)
          .publicKey(publicJsonWebKey)
          .keySize(request.getKeySize())
          .build();
    }
  }

  private KeyPair getRsaKeyPair(Integer keySize) {
    return switch (keySize) {
      case 4096 -> {
        log.debug("Generating 4096-bit RSA key pair");
        yield JWTUtils.generate4096_RSAKeyPair();
      }
      case 3072 -> {
        log.debug("Generating 3072-bit RSA key pair");
        yield JWTUtils.generate3072_RSAKeyPair();
      }
      default -> {
        log.debug("Generating 2048-bit RSA key pair");
        yield JWTUtils.generate2048_RSAKeyPair();
      }
    };
  }
}
