package com.dreamsportslabs.guardian.jwtVerifier;

import com.dreamsportslabs.guardian.jwtVerifier.exception.PublicKeysManagerException;
import io.fusionauth.jwks.JSONWebKeySetHelper;
import io.fusionauth.jwks.domain.JSONWebKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class PublicKeysManager {
  private static final long REFRESH_SKEW_MILLIS = 120000L;
  private String publicCertsEncodedUrl;
  private Long refreshTimeout;
  private Long expirationTime;
  private Map<String, PublicKey> publicKeys;
  private Lock lock;

  PublicKeysManager(Builder builder) {
    if (builder.publicKeys == null && builder.publicCertsEncodedUrl == null) {
      throw new PublicKeysManagerException(
          "Either publicKeys or publicCertsEncodedUrl must be provided.");
    }

    if (builder.publicKeys != null) {
      this.publicKeys = new HashMap<>();
      for (Map.Entry<String, String> key : builder.publicKeys.entrySet()) {
        this.publicKeys.put(key.getKey(), JSONWebKey.parse(JSONWebKey.build(key.getValue())));
      }
      return;
    }

    this.publicCertsEncodedUrl = builder.publicCertsEncodedUrl;
    this.refreshTimeout = builder.refreshTimeout;

    this.lock = new ReentrantLock();
  }

  static Builder builder() {
    return new Builder();
  }

  Map<String, PublicKey> getPublicKeyMap() {
    if (this.publicCertsEncodedUrl == null) {
      return this.publicKeys;
    }

    this.lock.lock();
    try {
      if (this.publicKeys == null || this.isExpired()) {
        this.refresh();
      }
    } finally {
      this.lock.unlock();
    }

    return this.publicKeys;
  }

  private boolean isExpired() {
    return System.currentTimeMillis() + REFRESH_SKEW_MILLIS > this.expirationTime;
  }

  private void refresh() {
    this.lock.lock();

    try {
      this.publicKeys = new HashMap<>();

      for (JSONWebKey key : JSONWebKeySetHelper.retrieveKeysFromJWKS(this.publicCertsEncodedUrl)) {
        this.publicKeys.put(key.kid, JSONWebKey.parse(key));
      }

      this.expirationTime = System.currentTimeMillis() + this.refreshTimeout * 1000;
    } catch (Exception e) {
      throw new PublicKeysManagerException("Failed to create public keys from public certs url", e);
    } finally {
      this.lock.unlock();
    }
  }

  static class Builder {
    String publicCertsEncodedUrl;
    Map<String, String> publicKeys;
    Long refreshTimeout;

    Builder() {}

    PublicKeysManager build() {
      return new PublicKeysManager(this);
    }

    Builder publicCertsEncodedUrl(String publicCertsEncodedUrl) {
      this.publicCertsEncodedUrl = publicCertsEncodedUrl;
      return this;
    }

    Builder publicKeys(Map<String, String> publicKeys) {
      this.publicKeys = publicKeys;
      return this;
    }

    Builder refreshTimeout(long refreshTimeout) {
      this.refreshTimeout = refreshTimeout;
      return this;
    }
  }
}
