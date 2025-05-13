package com.dreamsportslabs.guardian.service.impl.idproviders;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_REQUEST;

import com.dreamsportslabs.guardian.config.tenant.GoogleConfig;
import com.dreamsportslabs.guardian.service.IdProvider;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Base64;
import java.util.Collections;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GoogleIdProvider implements IdProvider {
  private final GoogleIdTokenVerifier verifier;

  public GoogleIdProvider(GoogleConfig config) throws GeneralSecurityException, IOException {
    verifier =
        new GoogleIdTokenVerifier.Builder(
                GoogleNetHttpTransport.newTrustedTransport(), GsonFactory.getDefaultInstance())
            .setAudience(Collections.singletonList(config.getClientId()))
            .build();
  }

  @Override
  public Single<JsonObject> getUserIdentity(String idTokenString) {
    GoogleIdToken idToken;

    try {
      idToken = verifier.verify(idTokenString);
    } catch (Exception e) {
      throw INVALID_REQUEST.getCustomException("Invalid id token");
    }

    if (idToken == null) {
      throw INVALID_REQUEST.getCustomException("Invalid id token");
    }

    String[] parts = idTokenString.split("\\.");
    String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]));

    return Single.just(new JsonObject(payloadJson));
  }
}
