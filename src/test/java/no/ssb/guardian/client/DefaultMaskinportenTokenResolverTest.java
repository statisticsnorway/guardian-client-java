package no.ssb.guardian.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultMaskinportenTokenResolverTest {
    private static final String DUMMY_MASKINPORTEN_CLIENT_ID = "2fed8b0e-2d18-4eb9-81b2-fee27448518d";
    private static final URI DUMMY_URL_ROOT = URI.create("http://localhost:12345");
    private static final Set DUMMY_SCOPES = Set.of("some:scope1", "some:scope2");

    private DefaultMaskinportenTokenResolver resolver;

    @BeforeEach
    public void init() {
        GuardianClientConfig config = GuardianClientConfig.builder()
                .guardianUrl(DUMMY_URL_ROOT)
                .maskinportenClientId(DUMMY_MASKINPORTEN_CLIENT_ID)
                .keycloakUrl(DUMMY_URL_ROOT)
                .keycloakClientSecret("secret".toCharArray())
                .shortenedTokenExpirationInSeconds(0)
                .build();
        this.resolver = new DefaultMaskinportenTokenResolver(config);
    }

    @Test
    void serviceUser_getAccessTokenRequestBody_clientIdShouldNotBeSet() {
        String requestJson = Util.toJson(resolver.getAccessTokenRequestBody(true, DUMMY_SCOPES));
        assertThat(requestJson).doesNotContain("maskinportenClientId");
    }

    @Test
    void personalUser_getAccessTokenRequestBody_clientIdShouldBeSet() {
        String requestJson = Util.toJson(resolver.getAccessTokenRequestBody(false, DUMMY_SCOPES));
        assertThat(requestJson).contains("maskinportenClientId");
    }

    @Test
    void multipleScopes_getAccessTokenRequestBody_scopesShouldBeSet() {
        String requestJson = Util.toJson(resolver.getAccessTokenRequestBody(true, DUMMY_SCOPES));
        assertThat(requestJson).contains("scopes", "some:scope1", "some:scope2");
    }

    @Test
    void nullScopes_getAccessTokenRequestBody_scopesShouldNotBeSet() {
        String requestJson = Util.toJson(resolver.getAccessTokenRequestBody(true, null));
        assertThat(requestJson).doesNotContain("scopes");
    }

    @Test
    void emptyScopes_getAccessTokenRequestBody_scopesShouldNotBeSet() {
        String requestJson = Util.toJson(resolver.getAccessTokenRequestBody(true, Collections.EMPTY_SET));
        assertThat(requestJson).doesNotContain("scopes");
    }

}