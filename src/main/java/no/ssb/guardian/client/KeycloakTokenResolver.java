package no.ssb.guardian.client;

public interface KeycloakTokenResolver {

    /**
     * Retrieve keycloak access token
     */
    AccessTokenWrapper getKeycloakAccessToken();

}
