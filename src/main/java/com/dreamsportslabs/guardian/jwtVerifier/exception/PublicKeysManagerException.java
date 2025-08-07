package com.dreamsportslabs.guardian.jwtVerifier.exception;

public class PublicKeysManagerException extends RuntimeException {
  public PublicKeysManagerException(String message) {
    super(message == null ? "Failed to build Public Key Manager" : message);
  }

  public PublicKeysManagerException(String message, Throwable cause) {
    super(message == null ? "Failed to build Public Key Manager" : message, cause);
  }
}
