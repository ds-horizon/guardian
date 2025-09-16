package com.dreamsportslabs.guardian.service;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_REQUEST;

import com.dreamsportslabs.guardian.dao.ClientDao;
import com.dreamsportslabs.guardian.dao.ClientScopeDao;
import com.dreamsportslabs.guardian.dao.model.ClientScopeModel;
import com.dreamsportslabs.guardian.dto.request.CreateClientScopeRequestDto;
import com.dreamsportslabs.guardian.dto.request.scope.GetScopeRequestDto;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class ClientScopeService {
  private final ClientScopeDao clientScopeDao;
  private final ScopeService scopeService;
  private final ClientDao clientDao;

  public Completable createClientScope(
      String clientId, CreateClientScopeRequestDto requestDto, String tenantId) {
    return clientDao
        .getClient(clientId, tenantId)
        .switchIfEmpty(Single.error(INVALID_REQUEST.getCustomException("Client not found")))
        .flatMap(
            client -> {
              GetScopeRequestDto getScopeRequestDto = new GetScopeRequestDto();
              getScopeRequestDto.setNames(requestDto.getScopes());
              return scopeService
                  .getScopes(tenantId, getScopeRequestDto)
                  .flatMap(
                      scopeModelList -> {
                        if (scopeModelList.isEmpty()) {
                          return Single.error(
                              INVALID_REQUEST.getCustomException("No valid scopes found"));
                        }
                        HashSet<String> inputScopes = new HashSet<>(requestDto.getScopes());

                        if (inputScopes.size() != scopeModelList.size()) {
                          return Single.error(
                              INVALID_REQUEST.getCustomException("Some scopes do not exist"));
                        }
                        return Single.just(client);
                      });
            })
        .flatMapCompletable(
            client -> {
              HashSet<String> scopesSet = new HashSet<>(requestDto.getScopes());
              // Add scope one by one to client scope table
              List<ClientScopeModel> clientScopeList = new ArrayList<>();
              for (String scope : scopesSet) {
                ClientScopeModel clientScopeModel =
                    ClientScopeModel.builder()
                        .tenantId(tenantId)
                        .clientId(clientId)
                        .scope(scope)
                        .build();
                clientScopeList.add(clientScopeModel);
              }
              return clientScopeDao.createClientScope(clientScopeList);
            });
  }

  public Single<List<ClientScopeModel>> getClientScopes(String clientId, String tenantId) {
    return clientDao
        .getClient(clientId, tenantId)
        .switchIfEmpty(Single.error(INVALID_REQUEST.getCustomException("Client not found")))
        .flatMap(client -> clientScopeDao.getClientScopes(clientId, tenantId));
  }

  public Completable deleteClientScope(String clientId, String scope, String tenantId) {
    return clientDao
        .getClient(clientId, tenantId)
        .switchIfEmpty(Single.error(INVALID_REQUEST.getCustomException("Client not found")))
        .flatMapCompletable(exists -> clientScopeDao.deleteClientScope(tenantId, clientId, scope));
  }

  public Completable validateClientScopes(
      String tenantId, String clientId, List<String> requestScopes) {
    return getClientScopes(clientId, tenantId)
        .map(
            clientScopeModels ->
                clientScopeModels.stream().map(ClientScopeModel::getScope).toList())
        .flatMapCompletable(
            clientScopes -> {
              HashSet<String> scopesSet = new HashSet<>(clientScopes);
              if (scopesSet.containsAll(requestScopes)) {
                return Completable.complete();
              } else {
                return Completable.error(
                    INVALID_REQUEST.getCustomException("Some scopes do not exist"));
              }
            });
  }
}
