package com.dreamsportslabs.guardian.dto;

import com.dreamsportslabs.guardian.dao.model.PasswordlessModel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PasswordlessContext {
  private final PasswordlessModel model;
  private final String userIdentifier;
  private final Integer globalResendCount;
}
