package com.dreamsportslabs.guardian.service;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.CLIENT_NOT_FOUND;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_CLIENT;
import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_REQUEST;

import com.dreamsportslabs.guardian.constant.ClientType;
import com.dreamsportslabs.guardian.dao.ClientDao;
import com.dreamsportslabs.guardian.dao.model.ClientModel;
import com.dreamsportslabs.guardian.dto.request.CreateClientRequestDto;
import com.dreamsportslabs.guardian.dto.request.UpdateClientRequestDto;
import com.dreamsportslabs.guardian.exception.OidcErrorEnum;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class ClientService {
  private static final int CLIENT_ID_LENGTH = 20;
  private static final int CLIENT_SECRET_LENGTH = 32;

  private final ClientDao clientDao;
  private final ClientScopeService clientScopeService;

  public Single<ClientModel> createClient(CreateClientRequestDto requestDto, String tenantId) {
    String clientId = RandomStringUtils.randomAlphanumeric(CLIENT_ID_LENGTH);
    String clientSecret = RandomStringUtils.randomAlphanumeric(CLIENT_SECRET_LENGTH);

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
            .clientType(requestDto.getClientType().getValue())
            .isDefault(requestDto.getIsDefault())
            .build();

    return clientDao.createClient(clientModel);
  }

  public Single<ClientModel> getClient(String clientId, String tenantId) {
    return clientDao
        .getClient(clientId, tenantId)
        .switchIfEmpty(Single.error(CLIENT_NOT_FOUND.getException()));
  }

  public Single<List<ClientModel>> getClients(String tenantId, int page, int limit) {
    int offset = (page - 1) * limit;
    return clientDao.getClients(tenantId, limit, offset);
  }

  public Single<ClientModel> updateClient(
      String clientId, UpdateClientRequestDto requestDto, String tenantId) {
    return clientDao
        .updateClient(requestDto, clientId, tenantId)
        .andThen(getClient(clientId, tenantId));
  }

  public Completable deleteClient(String clientId, String tenantId) {
    return clientDao
        .deleteClient(clientId, tenantId)
        .filter(deleted -> deleted)
        .switchIfEmpty(Single.error(INVALID_REQUEST.getCustomException("Client not found")))
        .ignoreElement();
  }

  public Single<String> regenerateClientSecret(String clientId, String tenantId) {
    return clientDao
        .getClient(clientId, tenantId)
        .switchIfEmpty(Single.error(INVALID_REQUEST.getCustomException("Client not found")))
        .flatMap(
            existingClient -> {
              String newSecret = RandomStringUtils.randomAlphanumeric(CLIENT_SECRET_LENGTH);
              existingClient.setClientSecret(newSecret);
              return clientDao
                  .updateClientSecret(newSecret, clientId, tenantId)
                  .andThen(Single.just(newSecret));
            });
  }

  public Single<ClientModel> authenticateClient(
      String clientId, String clientSecret, String tenantId) {
    return getClient(clientId, tenantId)
        .onErrorResumeNext(err -> Single.error(OidcErrorEnum.INVALID_CLIENT.getException()))
        .filter(clientModel -> clientModel.getClientSecret().equals(clientSecret))
        .switchIfEmpty(Single.error(OidcErrorEnum.INVALID_CLIENT.getException()));
  }

  public Completable validateFirstPartyClient(String tenantId, String clientId) {
    return getClient(clientId, tenantId)
        .onErrorResumeNext(err -> Single.error(INVALID_CLIENT.getException()))
        .filter(
            clientModel -> clientModel.getClientType().equals(ClientType.FIRST_PARTY.getValue()))
        .switchIfEmpty(Single.error(INVALID_CLIENT.getException()))
        .ignoreElement();
  }

  public Completable validateFirstPartyClientAndClientScopes(
      String tenantId, String clientId, List<String> requestScopes) {
    return validateFirstPartyClient(tenantId, clientId)
        .andThen(clientScopeService.validateClientScopes(tenantId, clientId, requestScopes));
  }

  public Single<String> getDefaultClientId(String tenantId) {
    return clientDao
        .getDefaultClient(tenantId)
        .filter(
            clientModel -> clientModel.getClientType().equals(ClientType.FIRST_PARTY.getValue()))
        .switchIfEmpty(Single.error(INVALID_CLIENT.getException()))
        .map(ClientModel::getClientId);
  }
}
