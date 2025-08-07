package com.dreamsportslabs.guardian.jwtVerifier.exception;

public class InvalidTokenException extends RuntimeException {
  public InvalidTokenException(String message) {
    super(message == null ? "Invalid Token" : message);
  }
}
