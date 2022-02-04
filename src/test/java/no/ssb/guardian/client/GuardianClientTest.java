package no.ssb.guardian.client;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.security.Guard;
import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

import static no.ssb.guardian.client.GuardianClientConfig.Environment.STAGING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class GuardianClientTest {

    private static final String DUMMY_MASKINPORTEN_CLIENT_ID = "2fed8b0e-2d18-4eb9-81b2-fee27448518d";
    private static final URI DUMMY_URL_ROOT = URI.create("http://localhost:12345");
    private static final int _3_SECONDS = 3000;
    private static final int _4_SECONDS = 4000;
    private static final int _30_SECONDS = 30000;
    private static final int _60_SECONDS = 60000;
    private static AccessTokenWrapper dummyAccessToken(String id, int millisUntilExpiration) {
        return new AccessTokenWrapper(id,
                id + "-body-" + UUID.randomUUID(),
                Instant.now().plusMillis(millisUntilExpiration));
    }

    private static String requiredEnv(String name) {
        return Optional.ofNullable(System.getenv(name))
                .orElseThrow(() -> new IllegalArgumentException("Error: Please set the " + name + " environment variable"));
    }

    /**
     * Set the $TEST_MASKINPORTEN_CLIENT_ID and $TEST_KEYCLOAK_CLIENT_SECRET environment variables to test against
     * the live staging endpoint
     */
    @Test
    @Disabled("Only used for manual testing against real endpoints")
    void serviceAccountKeycloakToken_getMaskinportenToken_shouldReturnToken() {
        assertThat(requiredEnv("TEST_MASKINPORTEN_CLIENT_ID")).isNotBlank();
        assertThat(requiredEnv("TEST_KEYCLOAK_CLIENT_SECRET")).isNotBlank();

        GuardianClient client = new GuardianClient(GuardianClientConfig.builder()
                .environment(STAGING)
                .maskinportenClientId(requiredEnv("TEST_MASKINPORTEN_CLIENT_ID"))
                .keycloakClientSecret(requiredEnv("TEST_KEYCLOAK_CLIENT_SECRET").toCharArray())
                .build());

        String token = client.getMaskinportenAccessToken();
        System.out.println(token);
    }

    /**
     * Set the $TEST_KEYCLOAK_TOKEN environment variable to test against the live staging endpoint
     */
    @Test
    @Disabled("Only used for manual testing against real endpoints")
    void staticKeycloakToken_getMaskinportenToken_shouldReturnToken() {
        assertThat(requiredEnv("TEST_MASKINPORTEN_CLIENT_ID")).isNotBlank();
        assertThat(requiredEnv("TEST_KEYCLOAK_TOKEN")).isNotBlank();

        GuardianClient client = new GuardianClient(GuardianClientConfig.builder()
                .environment(STAGING)
                .maskinportenClientId(requiredEnv("TEST_MASKINPORTEN_CLIENT_ID"))
                .staticKeycloakToken(requiredEnv("TEST_KEYCLOAK_TOKEN"))
                .build());

        String token = client.getMaskinportenAccessToken();
        System.out.println(token);
    }

    @Test
    void guardianClient_getMaskinportenToken_shouldUseCachedAccessTokens() {
        GuardianClientConfig config = GuardianClientConfig.builder()
                .guardianUrl(DUMMY_URL_ROOT)
                .maskinportenClientId(DUMMY_MASKINPORTEN_CLIENT_ID)
                .keycloakUrl(DUMMY_URL_ROOT)
                .keycloakClientSecret("secret".toCharArray())
                .shortenedTokenExpirationInSeconds(0)
                .build();

        KeycloakTokenResolver keycloakTokenResolver = mock(KeycloakTokenResolver.class);
        when(keycloakTokenResolver.getKeycloakAccessToken())
                .thenAnswer(a -> dummyAccessToken(config.getKeycloakClientId(), _30_SECONDS));

        MaskinportenTokenResolver maskinportenTokenResolver = mock(MaskinportenTokenResolver.class);
        when(maskinportenTokenResolver.getMaskinportenAccessToken(any(), anySet()))
                .thenAnswer(a -> dummyAccessToken(config.getMaskinportenClientId(), _60_SECONDS));

        GuardianClient client = new GuardianClient(config, keycloakTokenResolver, maskinportenTokenResolver);

        // Get token twice - via the GuardianClient
        String token1 = client.getMaskinportenAccessToken();
        String token2 = client.getMaskinportenAccessToken();

        assertThat(token1).as("Tokens should be equal since the second invocation returns cached value")
                .isEqualTo(token2);

        // Validate that the client has only invoked the resolvers once
        verify(keycloakTokenResolver, times(1)).getKeycloakAccessToken();
        verify(maskinportenTokenResolver, times(1)).getMaskinportenAccessToken(any(), anySet());

        // Get token twice - using the MaskinportenTokenResolver directly
        String token3 = maskinportenTokenResolver.getMaskinportenAccessToken("blah", new HashSet<>()).getToken();
        String token4 = maskinportenTokenResolver.getMaskinportenAccessToken("blah", new HashSet<>()).getToken();
        assertThat(token3).as("Tokens should be different since the cache in not involved when invoking the resolver directly")
                .isNotEqualTo(token4);
    }

    @Test
    void guardianClient_getMaskinportenToken_shouldUseCachedAccessTokensAndRefetchIfExpired() throws Exception {
        GuardianClientConfig config = GuardianClientConfig.builder()
                .guardianUrl(DUMMY_URL_ROOT)
                .maskinportenClientId(DUMMY_MASKINPORTEN_CLIENT_ID)
                .keycloakUrl(DUMMY_URL_ROOT)
                .keycloakClientSecret("secret".toCharArray())
                .shortenedTokenExpirationInSeconds(0)
                .build();

        KeycloakTokenResolver keycloakTokenResolver = mock(KeycloakTokenResolver.class);
        when(keycloakTokenResolver.getKeycloakAccessToken())
                .thenAnswer(a -> dummyAccessToken(config.getKeycloakClientId(), _30_SECONDS));

        MaskinportenTokenResolver maskinportenTokenResolver = mock(MaskinportenTokenResolver.class);
        when(maskinportenTokenResolver.getMaskinportenAccessToken(any(), anySet()))
                .thenAnswer(a -> dummyAccessToken(config.getMaskinportenClientId(), _3_SECONDS));

        GuardianClient client = new GuardianClient(config, keycloakTokenResolver, maskinportenTokenResolver);

        // Get token twice - via the GuardianClient
        String token1 = client.getMaskinportenAccessToken();
        String token2 = client.getMaskinportenAccessToken();

        assertThat(token1).as("Tokens should be equal since the second invocation returns cached value")
                .isEqualTo(token2);

        // Validate that the client has only invoked the resolvers once
        verify(keycloakTokenResolver, times(1)).getKeycloakAccessToken();
        verify(maskinportenTokenResolver, times(1)).getMaskinportenAccessToken(any(), anySet());

        Thread.sleep(_4_SECONDS);
        String token3 = client.getMaskinportenAccessToken();
        assertThat(token3).as("Tokens should not be equal since the cached token was expired and thus should have been refetched")
                .isNotEqualTo(token1);

        // Validate that the client has invoked the maskinportenTokenResolver again
        // The keycloakTokenResolver should not have been invoked since the keycloak token is still not expired
        verify(keycloakTokenResolver, times(1)).getKeycloakAccessToken();
        verify(maskinportenTokenResolver, times(2)).getMaskinportenAccessToken(any(), anySet());

        String token4 = client.getMaskinportenAccessToken();
        assertThat(token3).as("Tokens should be equal since the fourth invocation returns cached value")
                .isEqualTo(token4);
    }

    @Test
    void testStuff() {
        GuardianClientConfig config = GuardianClientConfig.builder()
                .guardianUrl(DUMMY_URL_ROOT)
                .maskinportenClientId(DUMMY_MASKINPORTEN_CLIENT_ID)
                .keycloakUrl(DUMMY_URL_ROOT)
                .keycloakClientSecret("secret".toCharArray())
                .shortenedTokenExpirationInSeconds(0)
                .build();

        GuardianClient client = GuardianClient.withConfig()
                .environment(STAGING)
                .maskinportenClientId("some-uuid")
                .keycloakClientSecret("some-secret")
                .create();

        String accessToken = client.getMaskinportenAccessToken("some:scope1", "some:scope2");
        // ...

    }
}
