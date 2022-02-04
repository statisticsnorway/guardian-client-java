package no.ssb.guardian.client;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static no.ssb.guardian.client.GuardianClient.VERBOSE;
import static no.ssb.guardian.client.Util.base64EncodedCredentials;

@Slf4j
public class DefaultKeycloakTokenResolver implements KeycloakTokenResolver {

    private final HttpClient httpClient;
    private final GuardianClientConfig config;

    public DefaultKeycloakTokenResolver(@NonNull GuardianClientConfig config) {
        this.httpClient = HttpClient.newHttpClient();
        this.config = config;
    }

    @Override
    public AccessTokenWrapper getKeycloakAccessToken() {
        String keycloakClientId = "maskinporten-" + config.getMaskinportenClientId();
        log.debug(VERBOSE, "Get keycloak access token for client ID " + keycloakClientId);
        String params = "grant_type=" + URLEncoder.encode("client_credentials", StandardCharsets.UTF_8);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(config.getKeycloakUrl().resolve(config.getKeycloakTokenEndpoint()))
                .header("User-Agent", GuardianClient.userAgent())
                .header( "Content-Type", "application/x-www-form-urlencoded")
                .header("Authorization", "Basic " + base64EncodedCredentials(keycloakClientId, config.getKeycloakClientSecret()))
                .POST(HttpRequest.BodyPublishers.ofString(params))
                .build();

        final HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        }
        catch (IOException|InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new GuardianClientException(String.format(
                    "Error fetching keycloak token for %s", keycloakClientId
            ), e);
        }

        if (response.statusCode() != 200) {
            throw new GuardianClientException(String.format(
                    "Error (%s) fetching keycloak token for %s: %s",
                    response.statusCode(),
                    keycloakClientId,
                    response.body()
            ));
        }

        Map<String, Object> resMap = Util.jsonToMap(response.body());
        String keycloakAccessToken =  (String) resMap.get("access_token");
        log.trace(VERBOSE, "Keycloak access token: " + keycloakAccessToken);
        return new AccessTokenWrapper(keycloakClientId, keycloakAccessToken);
    }

}
