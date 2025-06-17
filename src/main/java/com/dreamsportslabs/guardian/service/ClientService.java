package com.dreamsportslabs.guardian.service;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_REQUEST;

import com.dreamsportslabs.guardian.dao.ClientDao;
import com.dreamsportslabs.guardian.dao.ClientScopeDao;
import com.dreamsportslabs.guardian.dao.model.ClientModel;
import com.dreamsportslabs.guardian.dto.request.CreateClientRequestDto;
import com.dreamsportslabs.guardian.dto.request.UpdateClientRequestDto;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class ClientService {
  private final ClientDao clientDao;
  private final ClientScopeDao clientScopeDao;

  public Single<ClientModel> createClient(CreateClientRequestDto requestDto, String tenantId) {
    requestDto.validate();

    String clientId = RandomStringUtils.randomAlphanumeric(20);
    String clientSecret = RandomStringUtils.randomAlphanumeric(32);

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

    return clientDao.createClient(clientModel);
  }

  public Maybe<ClientModel> getClient(String clientId, String tenantId) {
    return clientDao.getClientById(clientId, tenantId);
  }

  public Single<List<ClientModel>> getClients(String tenantId, int page, int limit) {
    int offset = (page - 1) * limit;
    return clientDao.getClientsByTenant(tenantId, limit, offset);
  }

  public Single<ClientModel> updateClient(
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
            });
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
              String newSecret = RandomStringUtils.randomAlphanumeric(32);
              existingClient.setClientSecret(newSecret);
              return clientDao.updateClient(existingClient).map(updated -> newSecret);
            });
  }
}
