package no.ssb.guardian.client;

import java.util.Set;

public interface MaskinportenTokenResolver {

    /**
     * Retrieve maskinporten access token.
     *
     * @param keycloakToken user's keycloak token
     * @param scopes the maskinporten scopes. May be null or empty for service users
     */
    AccessTokenWrapper getMaskinportenAccessToken(String keycloakToken, Set<String> scopes);

}
