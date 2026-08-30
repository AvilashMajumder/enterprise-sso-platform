package com.enterprise.orgshield.service;

import com.enterprise.orgshield.config.KeycloakAdminProperties;
import com.enterprise.orgshield.entity.Application;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
public class KeycloakAdminService {

    private final WebClient webClient;
    private final KeycloakAdminProperties properties;

    public KeycloakAdminService(
            WebClient webClient,
            KeycloakAdminProperties properties) {

        this.webClient = webClient;
        this.properties = properties;
    }

    /*
     * Get an access token using the
     * orgshield-admin service account.
     */
    private String getAccessToken() {

        LinkedMultiValueMap<String, String> form =
                new LinkedMultiValueMap<>();

        form.add(
                "grant_type",
                "client_credentials"
        );

        form.add(
                "client_id",
                properties.getClientId()
        );

        form.add(
                "client_secret",
                properties.getClientSecret()
        );

        Map<?, ?> response =
                webClient
                        .post()
                        .uri(
                                properties.getServerUrl()
                                        + "/realms/"
                                        + properties.getRealm()
                                        + "/protocol/openid-connect/token"
                        )
                        .contentType(
                                MediaType.APPLICATION_FORM_URLENCODED
                        )
                        .bodyValue(form)
                        .retrieve()
                        .bodyToMono(Map.class)
                        .block();

        if (response == null ||
                response.get("access_token") == null) {

            throw new IllegalStateException(
                    "Unable to obtain Keycloak access token"
            );
        }

        return response
                .get("access_token")
                .toString();
    }


    /*
     * Create a client in Keycloak.
     */
    public void createClient(Application application) {

        String token = getAccessToken();

        Map<String, Object> client =
                Map.of(
                        "clientId",
                        application.getClientId(),

                        "name",
                        application.getName(),

                        "enabled",
                        true,

                        "protocol",
                        "openid-connect",

                        "publicClient",
                        false,

                        "standardFlowEnabled",
                        true,

                        "redirectUris",
                        new String[]{
                                application.getRedirectUri()
                        },

                        "attributes",
                        Map.of(
                                "post.logout.redirect.uris",
                                application
                                        .getPostLogoutRedirectUri()
                        )
                );

        webClient
                .post()
                .uri(
                        properties.getServerUrl()
                                + "/admin/realms/"
                                + properties.getRealm()
                                + "/clients"
                )
                .headers(headers ->
                        headers.setBearerAuth(token)
                )
                .contentType(
                        MediaType.APPLICATION_JSON
                )
                .bodyValue(client)
                .retrieve()
                .toBodilessEntity()
                .block();
    }


    /*
     * Delete a client from Keycloak.
     *
     * The clientId stored in our database
     * is NOT the internal Keycloak UUID.
     *
     * Therefore:
     *
     * demo-app-1
     *      ↓
     * Find Keycloak client
     *      ↓
     * Get internal UUID
     *      ↓
     * DELETE /clients/{UUID}
     */
    public void deleteClient(String clientId) {

        String token = getAccessToken();

        String keycloakClientId =
                getKeycloakClientId(
                        clientId,
                        token
                );

        webClient
                .delete()
                .uri(
                        properties.getServerUrl()
                                + "/admin/realms/"
                                + properties.getRealm()
                                + "/clients/"
                                + keycloakClientId
                )
                .headers(headers ->
                        headers.setBearerAuth(token)
                )
                .retrieve()
                .toBodilessEntity()
                .block();
    }


    /*
     * Find the internal Keycloak UUID
     * using the application's clientId.
     */
    private String getKeycloakClientId(
            String clientId,
            String token) {

        String url =
                properties.getServerUrl()
                        + "/admin/realms/"
                        + properties.getRealm()
                        + "/clients?clientId="
                        + clientId;

        List<Map<String, Object>> clients =
                webClient
                        .get()
                        .uri(url)
                        .headers(headers ->
                                headers.setBearerAuth(token)
                        )
                        .retrieve()
                        .bodyToMono(List.class)
                        .block();

        if (clients == null ||
                clients.isEmpty()) {

            throw new IllegalArgumentException(
                    "Keycloak client not found: "
                            + clientId
            );
        }

        Object id =
                clients.get(0).get("id");

        if (id == null) {

            throw new IllegalStateException(
                    "Keycloak client has no internal ID"
            );
        }

        return id.toString();
    }
}