package com.dreamsportslabs.guardian.service;

import static com.dreamsportslabs.guardian.exception.ErrorEnum.INVALID_REQUEST;

import com.dreamsportslabs.guardian.dao.ClientDao;
import com.dreamsportslabs.guardian.dao.ClientScopeDao;
import com.dreamsportslabs.guardian.dao.model.ClientScopeModel;
import com.dreamsportslabs.guardian.dto.request.CreateClientScopeRequestDto;
import com.dreamsportslabs.guardian.dto.response.ClientScopeResponseDto;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Single;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class ClientScopeService {
  private final ClientScopeDao clientScopeDao;
  private final ScopeService scopeService;
  private final ClientDao clientDao;

  public Single<Boolean> createClientScope(
      String clientId, CreateClientScopeRequestDto requestDto, String tenantId) {
    requestDto.validate();

    // Verify client exists
    return clientDao
        .getClientById(clientId, tenantId)
        .switchIfEmpty(Single.error(INVALID_REQUEST.getCustomException("Client not found")))
        .flatMap(
            client ->
                // validate scope exists
                scopeService
                    .filterExistingScopes(tenantId, requestDto.getScopes())
                    .flatMap(
                        scopes -> {
                          // Check if the requested scopes exists in the tenant's scopes
                          if (scopes.isEmpty()) {
                            return Single.error(
                                INVALID_REQUEST.getCustomException("No valid scopes found"));
                          }
                          HashSet<String> inputScopes = new HashSet<>(requestDto.getScopes());
                          if (inputScopes.size() != scopes.size()) {
                            return Single.error(
                                INVALID_REQUEST.getCustomException("Some scopes do not exist"));
                          }
                          return Single.just(client);
                        }))
        .flatMap(
            client -> {
              HashSet<String> scopesSet = new HashSet<>(requestDto.getScopes());
              // Add scope one by one to client scope table
              List<Single<ClientScopeModel>> clientScopeSingles = new ArrayList<>();
              for (String scope : scopesSet) {
                ClientScopeModel clientScopeModel =
                    ClientScopeModel.builder()
                        .tenantId(tenantId)
                        .clientId(clientId)
                        .scope(scope)
                        .build();
                clientScopeSingles.add(clientScopeDao.createClientScope(clientScopeModel));
              }
              return Single.zip(clientScopeSingles, results -> true);
            });
  }

  public Single<ClientScopeResponseDto> getClientScopes(String clientId, String tenantId) {
    // Verify client exists
    return clientDao
        .getClientById(clientId, tenantId)
        .switchIfEmpty(Single.error(INVALID_REQUEST.getCustomException("Client not found")))
        .flatMap(
            client ->
                clientScopeDao
                    .getClientScopes(clientId, tenantId)
                    .map(
                        scopes -> {
                          ClientScopeResponseDto responseDto = new ClientScopeResponseDto();
                          responseDto.setScopes(
                              scopes.stream()
                                  .map(ClientScopeModel::getScope)
                                  .collect(Collectors.toList()));
                          return responseDto;
                        }));
  }

  public Single<Boolean> deleteClientScope(String clientId, String scope, String tenantId) {
    // Verify client exists
    return clientDao
        .getClientById(clientId, tenantId)
        .switchIfEmpty(Single.error(INVALID_REQUEST.getCustomException("Client not found")))
        .flatMap(clientModel -> getClientScopes(clientId, tenantId))
        .map(
            scopes -> {
              // Check if the scope exists for the client
              if (!scopes.getScopes().contains(scope)) {
                throw INVALID_REQUEST.getCustomException(
                    "Client scope does not exist for client: " + clientId);
              }
              return true;
            })
        .flatMap(exists -> clientScopeDao.deleteClientScope(tenantId, clientId, scope));
  }
}
