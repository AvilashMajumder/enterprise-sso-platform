package com.enterprise.orgshield.service;

import com.enterprise.orgshield.config.KeycloakAdminProperties;
import com.enterprise.orgshield.entity.Application;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

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

    public void createClient(Application application) {

        String accessToken = getAdminAccessToken();

        Map<String, Object> client = Map.of(
                "clientId", application.getClientId(),
                "enabled", true,
                "protocol", "openid-connect",
                "publicClient", false,
                "redirectUris",
                new String[]{
                        application.getRedirectUri()
                },
                "webOrigins",
                new String[]{
                        application.getRedirectUri()
                },
                "attributes",
                Map.of(
                        "post.logout.redirect.uris",
                        application.getPostLogoutRedirectUri()
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
                        headers.setBearerAuth(accessToken)
                )
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(client)
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    private String getAdminAccessToken() {

        Map response = webClient
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
                .bodyValue(
                        "grant_type=client_credentials"
                                + "&client_id="
                                + properties.getClientId()
                                + "&client_secret="
                                + properties.getClientSecret()
                )
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (response == null ||
                response.get("access_token") == null) {

            throw new IllegalStateException(
                    "Unable to obtain Keycloak admin token"
            );
        }

        return response
                .get("access_token")
                .toString();
    }
}