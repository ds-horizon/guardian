package com.dreamsportslabs.guardian.service;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_REQUEST;

import com.dreamsportslabs.guardian.dao.ClientDao;
import com.dreamsportslabs.guardian.dao.ClientScopeDao;
import com.dreamsportslabs.guardian.dao.model.ClientModel;
import com.dreamsportslabs.guardian.dto.request.CreateClientRequestDto;
import com.dreamsportslabs.guardian.dto.request.UpdateClientRequestDto;
import com.dreamsportslabs.guardian.dto.response.ClientListResponseDto;
import com.dreamsportslabs.guardian.dto.response.ClientResponseDto;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class ClientService {
  private final ClientDao clientDao;
  private final ClientScopeDao clientScopeDao;
  private final SecureRandom secureRandom = new SecureRandom();

  public Single<ClientResponseDto> createClient(
      CreateClientRequestDto requestDto, String tenantId) {
    requestDto.validate();

    String clientId = UUID.randomUUID().toString().replace("-", "");
    String clientSecret = generateClientSecret();

    ClientModel clientModel =
        ClientModel.builder()
            .tenantId(tenantId)
            .clientId(clientId)
            .clientName(requestDto.getClientName())
            .clientSecret(clientSecret)
            .clientUri(requestDto.getClientUri())
            .contacts(requestDto.getContacts())
            .grantTypes(requestDto.getGrantTypes())
            .logoUri(requestDto.getLogoUri())
            .policyUri(requestDto.getPolicyUri())
            .redirectUris(requestDto.getRedirectUris())
            .responseTypes(requestDto.getResponseTypes())
            .skipConsent(requestDto.getSkipConsent())
            .build();

    return clientDao.createClient(clientModel).map(this::mapToResponseDto);
  }

  public Maybe<ClientResponseDto> getClient(String clientId, String tenantId) {
    return clientDao.getClientById(clientId, tenantId).map(this::mapToResponseDto);
  }

  public Single<ClientListResponseDto> getClients(String tenantId, int page, int limit) {
    if (page < 1) {
      throw INVALID_REQUEST.getCustomException("Page must be greater than 0");
    }
    if (limit < 1 || limit > 100) {
      throw INVALID_REQUEST.getCustomException("Limit must be between 1 and 100");
    }

    int offset = (page - 1) * limit;

    return clientDao
        .getClientsByTenant(tenantId, limit, offset)
        .map(
            clientModels ->
                ClientListResponseDto.builder()
                    .clients(clientModels.stream().map(this::mapToResponseDto).toList())
                    .limit(limit)
                    .page(page)
                    .build());
  }

  public Single<ClientResponseDto> updateClient(
      String clientId, UpdateClientRequestDto requestDto, String tenantId) {
    requestDto.validate();

    return clientDao
        .getClientById(clientId, tenantId)
        .switchIfEmpty(Single.error(INVALID_REQUEST.getCustomException("Client not found")))
        .flatMap(
            existingClient -> {
              ClientModel updatedClient =
                  ClientModel.builder()
                      .tenantId(tenantId)
                      .clientId(clientId)
                      .clientName(requestDto.getClientName())
                      .clientSecret(existingClient.getClientSecret()) // Keep existing secret
                      .clientUri(requestDto.getClientUri())
                      .contacts(requestDto.getContacts())
                      .grantTypes(requestDto.getGrantTypes())
                      .logoUri(requestDto.getLogoUri())
                      .policyUri(requestDto.getPolicyUri())
                      .redirectUris(requestDto.getRedirectUris())
                      .responseTypes(requestDto.getResponseTypes())
                      .skipConsent(requestDto.getSkipConsent())
                      .build();

              return clientDao.updateClient(updatedClient);
            })
        .map(this::mapToResponseDto);
  }

  public Single<Boolean> deleteClient(String clientId, String tenantId) {
    return clientDao
        .getClientById(clientId, tenantId)
        .switchIfEmpty(Single.error(INVALID_REQUEST.getCustomException("Client not found")))
        .flatMap(
            client ->
                clientScopeDao
                    .deleteClientScopesByClient(clientId, tenantId)
                    .flatMap(deleted -> clientDao.deleteClient(clientId, tenantId)));
  }

  public Single<String> regenerateClientSecret(String clientId, String tenantId) {
    return clientDao
        .getClientById(clientId, tenantId)
        .switchIfEmpty(Single.error(INVALID_REQUEST.getCustomException("Client not found")))
        .flatMap(
            existingClient -> {
              String newSecret = generateClientSecret();
              existingClient.setClientSecret(newSecret);
              return clientDao.updateClient(existingClient).map(updated -> newSecret);
            });
  }

  private String generateClientSecret() {
    byte[] randomBytes = new byte[32];
    secureRandom.nextBytes(randomBytes);
    return Base64.getUrlEncoder()
        .withoutPadding()
        .encodeToString(randomBytes)
        .replaceAll("[^A-Za-z0-9]", "");
  }

  private ClientResponseDto mapToResponseDto(ClientModel model) {
    return ClientResponseDto.builder()
        .clientId(model.getClientId())
        .clientName(model.getClientName())
        .clientSecret(model.getClientSecret())
        .clientUri(model.getClientUri())
        .contacts(model.getContacts())
        .grantTypes(model.getGrantTypes())
        .logoUri(model.getLogoUri())
        .policyUri(model.getPolicyUri())
        .redirectUris(model.getRedirectUris())
        .responseTypes(model.getResponseTypes())
        .skipConsent(model.getSkipConsent())
        .build();
  }
}
