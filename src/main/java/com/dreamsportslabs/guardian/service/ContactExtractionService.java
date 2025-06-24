package com.dreamsportslabs.guardian.service;

import com.dreamsportslabs.guardian.constant.Contact;
import com.dreamsportslabs.guardian.dao.PasswordlessDao;
import com.dreamsportslabs.guardian.dto.request.V1PasswordlessCompleteRequestDto;
import com.dreamsportslabs.guardian.dto.request.V1PasswordlessInitRequestDto;
import com.dreamsportslabs.guardian.dto.request.V1SignInRequestDto;
import com.dreamsportslabs.guardian.dto.request.V1SignUpRequestDto;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class ContactExtractionService {

  private final PasswordlessDao passwordlessDao;

  /** Extract contact information from passwordless init request */
  public List<String> extractContactsFromPasswordlessInit(V1PasswordlessInitRequestDto requestDto) {
    if (requestDto.getContacts() == null || requestDto.getContacts().isEmpty()) {
      return List.of();
    }

    return requestDto.getContacts().stream()
        .map(Contact::getIdentifier)
        .filter(StringUtils::isNotBlank)
        .toList();
  }

  /**
   * Extract contact information from sign in request For sign in, the username could be email or
   * phone number
   */
  public List<String> extractContactsFromSignIn(V1SignInRequestDto requestDto) {
    if (StringUtils.isNotBlank(requestDto.getUsername())) {
      return List.of(requestDto.getUsername());
    }
    return List.of();
  }

  /**
   * Extract contact information from sign up request For sign up, the username could be email or
   * phone number
   */
  public List<String> extractContactsFromSignUp(V1SignUpRequestDto requestDto) {
    if (StringUtils.isNotBlank(requestDto.getUsername())) {
      return List.of(requestDto.getUsername());
    }
    return List.of();
  }

  /**
   * Extract contact information from passwordless complete request This requires looking up the
   * state to get the original contact information
   */
  public Single<List<String>> extractContactsFromPasswordlessComplete(
      V1PasswordlessCompleteRequestDto requestDto, String tenantId) {
    if (StringUtils.isBlank(requestDto.getState())) {
      return Single.just(List.of());
    }

    return passwordlessDao
        .getPasswordlessModel(requestDto.getState(), tenantId)
        .map(
            model -> {
              if (model.getContacts() == null || model.getContacts().isEmpty()) {
                return List.<String>of();
              }

              return model.getContacts().stream()
                  .map(Contact::getIdentifier)
                  .filter(StringUtils::isNotBlank)
                  .toList();
            })
        .onErrorReturnItem(List.of())
        .toSingle();
  }

  /** Check if a given string looks like an email */
  public boolean isEmail(String contact) {
    return StringUtils.isNotBlank(contact) && contact.contains("@");
  }

  /** Check if a given string looks like a phone number */
  public boolean isPhoneNumber(String contact) {
    return StringUtils.isNotBlank(contact) && contact.matches("^\\d+$");
  }
}
