package no.ssb.guardian.client;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.net.URI;
import java.util.Optional;

import static no.ssb.guardian.client.GuardianClientConfig.Environment.*;

@Builder
@Data
public class GuardianClientConfig {

    /**
     * maskinportenClientId is the maskinporten client ID from samhandlingsportalen, typically a UUID
     */
    @NonNull
    private final String maskinportenClientId;

    /**
     * environment optionally denotes the runtime environment (PROD, STAGING or LOCAL) the Guardian Client is working
     * with. By specifying this you can rely on default values for guardianUrl and keycloakUrl
     */
    private final Environment environment;

    /**
     * internalAccess should be true if maskinporten guardian will only be accessed via its internal endpoints.
     * This is used for deducing ´guardianUrl´.
     *
     * </p>Defaults to false</p>
     */
    @Builder.Default
    private final boolean internalAccess = false;

    /**
     * guardianUrl is the root URL of the maskinporten-guardian server
     * e.g. https://maskinporten-guardian.staging-bip-app.ssb.no
     *
     * <p>You can alternatively specify the {@link Environment} to rely on defaults</p>
     */
    private final URI guardianUrl;

    public URI getGuardianUrl() {
        if (guardianUrl != null) {
            return guardianUrl;
        }

        if (internalAccess) {
            return URI.create("http://maskinporten-guardian.dapla.svc.cluster.local");
        }
        else if (environment == PROD) {
            return URI.create("https://guardian.dapla.ssb.no");
        }
        else if (environment == STAGING) {
            return URI.create("https://guardian.dapla-staging.ssb.no");
        }
        else if (environment == LOCAL) {
            return URI.create("http://localhost:10310");
        }
        else {
            throw new NullPointerException("Missing guardianUrl");
        }
    }

    /**
     * keycloakUrl is the root URL of the keycloak server
     * e.g. https://keycloak.staging-bip-app.ssb.no
     *
     * <p>You can alternatively specify the {@link Environment} to rely on defaults</p>
     */
    private final URI keycloakUrl;


    public URI getKeycloakUrl() {
        if (guardianUrl != null) {
            return guardianUrl;
        }

        if (environment == PROD) {
            return URI.create("https://keycloak.prod-bip-app.ssb.no");
        }
        else if (environment == STAGING) {
            return URI.create("https://keycloak.staging-bip-app.ssb.no");
        }
        else if (environment == LOCAL) {
            return URI.create("http://keycloak.staging-bip-app.ssb.no");
        }
        else {
            throw new NullPointerException("Missing keycloakUrl");
        }
    }

    /**
     * keycloakTokenEndpoint is the path to the token endpoint of the keycloak server. You will typically
     * not need to configure this unless you're using a non-standard endpoint. Defaults to
     * /auth/realms/ssb/protocol/openid-connect/token
     */
    @NonNull
    @Builder.Default
    private final String keycloakTokenEndpoint = "/auth/realms/ssb/protocol/openid-connect/token";

    /**
     * <p>keycloakClientId is the keycloak client credentials id for the keycloak
     * service user. This is used for retrieving a keycloak access token.</p>
     *
     * </p>Defaults to (maskinporten-[maskinportenClientId]).</p>
     *
     * </p>You will typically want to rely on the default value for this.</p>
     */
    private final String keycloakClientId;

    public String getKeycloakClientId() {
        return Optional.ofNullable(keycloakClientId).orElse("maskinporten-" + maskinportenClientId);
    }

    /**
     * keycloakClientSecret is the client credentials secret for the maskinporten keycloak
     * service user. This is used for retrieving a keycloak access token.
     */
    private final char[] keycloakClientSecret;

    /**
     * <p>staticKeycloakToken can be specified if you want to explicitly use a specific keycloak token. By setting this,
     * the guardian client will not attempt to retrieve any keycloak tokens. Note that you are responsible for making
     * sure the provided keycloak token is valid (not expired) and refreshed whenever needed.</p>
     *
     * <p>This option is primarily provided for "personal" users.</p>
     */
    private String staticKeycloakToken;

    /**
     * <p>shortenedTokenExpirationInSeconds is the number of seconds from actual token expiration time that the caches
     * will use to determine whether or not a token needs to be refetched.</p>
     * <p>Tokens are refetched if (currentTime + (tokenExpirationTime - shortenedTokenExpirationInSeconds)) <= 0
     * and the cache will be updated.</p>
     *
     * <p>Defaults to 300 seconds (5 minutes) - this way tokens will always have at least 5 minutes until expiration.</p>
     *
     * <p>You will typically want to configure this if your operation takes longer than 5 minutes to complete.</p>
     *
     * <p>Set this to 0 if you don't want the caches to simply use the token's expiration time. However, by doing this you
     * might run into "racy cases" where the Guardian Client returns a cached token that is just about to expire.</p>
     */
    @Builder.Default
    private final int shortenedTokenExpirationInSeconds = 300; // 5 minutes

    public static class GuardianClientConfigBuilder {

        public GuardianClientConfigBuilder guardianUrl(String guardianUrl) {
            return guardianUrl(URI.create(guardianUrl));
        }
        public GuardianClientConfigBuilder guardianUrl(URI guardianUrl) {
            this.guardianUrl = guardianUrl;
            return this;
        }
        public GuardianClientConfigBuilder keycloakUrl(String keycloakUrl) {
            return keycloakUrl(URI.create(keycloakUrl));
        }
        public GuardianClientConfigBuilder keycloakUrl(URI keycloakUrl) {
            this.keycloakUrl = keycloakUrl;
            return this;
        }
        public GuardianClientConfigBuilder keycloakClientSecret(String keycloakClientSecret) {
            return keycloakClientSecret(keycloakClientSecret.toCharArray());
        }
        public GuardianClientConfigBuilder keycloakClientSecret(char[] keycloakClientSecret) {
            this.keycloakClientSecret = keycloakClientSecret;
            return this;
        }


        public GuardianClient create() {
            return new GuardianClient(this.build());
        }
    }

    public enum Environment {
        PROD, STAGING, LOCAL
    }
}
