package no.ssb.guardian.client;

import io.jsonwebtoken.Claims;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.Set;

import static no.ssb.guardian.client.GuardianClient.VERBOSE;

@Slf4j
public class DefaultMaskinportenTokenResolver implements MaskinportenTokenResolver {

    private final HttpClient httpClient;
    private final GuardianClientConfig config;

    public DefaultMaskinportenTokenResolver(@NonNull GuardianClientConfig config) {
        this.httpClient = HttpClient.newHttpClient();
        this.config = config;
    }

    @Override
    public AccessTokenWrapper getMaskinportenAccessToken(@NonNull String keycloakToken, Set<String> scopes) {
        boolean isServiceUser = isServiceUser(keycloakToken);
        log.debug(VERBOSE, "getMaskinportenAccessToken (user type: {})", isServiceUser ? "service" : "personal");

        String requestBody = Util.toJson(getAccessTokenRequestBody(isServiceUser(keycloakToken), scopes));
        URI url = config.getGuardianUrl().resolve("/maskinporten/access-token");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .header("User-Agent", GuardianClient.userAgent())
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + keycloakToken)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
        final HttpResponse<String> response;
        log.debug(VERBOSE, "{}, {}", request, requestBody);
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        }
        catch (IOException|InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }

            log.trace("keycloakToken", keycloakToken);
            log.trace("requestBody", requestBody);
            throw new GuardianClientException(String.format(
                    "Error fetching maskinporten access token from %s for client id %s",
                    url,
                    config.getMaskinportenClientId()
            ), e);
        }

        if (response.statusCode() != 200) {
            throw new GuardianClientException(String.format(
                    "Error (%s) fetching maskinporten access token from %s for client id %s: %s",
                    response.statusCode(),
                    url,
                    config.getMaskinportenClientId(),
                    response.body()
            ));
        }

        Map<String, Object> resMap = Util.jsonToMap(response.body());
        return new AccessTokenWrapper(config.getMaskinportenClientId(), (String) resMap.get("accessToken"));
    }

    MaskinportenGuardianGetAccessTokenRequest getAccessTokenRequestBody(boolean isServiceUser, Set<String> scopes) {
        final MaskinportenGuardianGetAccessTokenRequest.MaskinportenGuardianGetAccessTokenRequestBuilder builder = MaskinportenGuardianGetAccessTokenRequest.builder();
        if (scopes != null && !scopes.isEmpty()) {
            builder.scopes(scopes);
        }
        // Only set maskinportenClientId explicitly for non-service (e.g. personal) users
        if (! isServiceUser) {
            builder.maskinportenClientId(config.getMaskinportenClientId());
        }

        return builder.build();
    }

    @Value
    @Builder
    private static class MaskinportenGuardianGetAccessTokenRequest {
        private final String maskinportenClientId;
        private final Set<String> scopes;
    }

    /**
     * Assume the keycloak access token to be a service user if it contains a maskinportenClientId claim
     */
    boolean isServiceUser(String keycloakToken) {
        Claims claims = Util.jwtClaimsOf(keycloakToken);
        boolean isValidKeycloakToken = claims.getIssuer().startsWith(config.getKeycloakUrl().toString());
        boolean hasServiceUserClaim = config.getMaskinportenClientId().equals(claims.get("maskinporten_client_id"));
        return isValidKeycloakToken && hasServiceUserClaim;
    }

}
