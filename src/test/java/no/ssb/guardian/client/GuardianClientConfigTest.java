package no.ssb.guardian.client;

import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GuardianClientConfigTest {
    private static final String DUMMY_MASKINPORTEN_CLIENT_ID = "2fed8b0e-2d18-4eb9-81b2-fee27448518d";

    @Test
    void deduceGuardianUrl_test_shouldUseExternalUrl() {
        GuardianClientConfig config = GuardianClientConfig.builder()
                .maskinportenClientId(DUMMY_MASKINPORTEN_CLIENT_ID)
                .environment(GuardianClientConfig.Environment.TEST)
                .build();

        assertThat(config.getGuardianUrl()).hasToString("https://guardian.dapla-staging.ssb.no");
    }

    @Test
    void deduceGuardianUrl_internalAccess_shouldUseInternalUrl() {
        GuardianClientConfig config = GuardianClientConfig.builder()
                .maskinportenClientId(DUMMY_MASKINPORTEN_CLIENT_ID)
                .environment(GuardianClientConfig.Environment.TEST)
                .internalAccess(true)
                .build();

        assertThat(config.getGuardianUrl()).hasToString("http://maskinporten-guardian.dapla.svc.cluster.local");
    }

    @Test
    void deduceKeycloakUrl_test_shouldUseTestKeycloakUrl() {
        GuardianClientConfig config = GuardianClientConfig.builder()
                .maskinportenClientId(DUMMY_MASKINPORTEN_CLIENT_ID)
                .environment(GuardianClientConfig.Environment.TEST)
                .build();

        assertThat(config.getKeycloakUrl()).hasToString("https://auth.test.ssb.no");
        assertThat(config.getKeycloakTokenEndpoint()).isEqualTo ("/realms/ssb/protocol/openid-connect/token");
    }

    @Test
    void deduceKeycloakUrl_prod_shouldUseTestKeycloakUrl() {
        GuardianClientConfig config = GuardianClientConfig.builder()
                .maskinportenClientId(DUMMY_MASKINPORTEN_CLIENT_ID)
                .environment(GuardianClientConfig.Environment.PROD)
                .build();

        assertThat(config.getKeycloakUrl()).hasToString("https://auth.ssb.no");
        assertThat(config.getKeycloakTokenEndpoint()).isEqualTo ("/realms/ssb/protocol/openid-connect/token");
    }

    @Test
    void deduceKeycloakUrl_bipStaging_shouldUseLegacyKeycloakUrl() {
        GuardianClientConfig config = GuardianClientConfig.builder()
                .maskinportenClientId(DUMMY_MASKINPORTEN_CLIENT_ID)
                .environment(GuardianClientConfig.Environment.STAGING_BIP)
                .build();

        assertThat(config.getKeycloakUrl()).hasToString("https://keycloak.staging-bip-app.ssb.no");
        assertThat(config.getKeycloakTokenEndpoint()).isEqualTo ("/auth/realms/ssb/protocol/openid-connect/token");
    }

    @Test
    void deduceKeycloakUrl_bipProd_shouldUseLegacyKeycloakUrl() {
        GuardianClientConfig config = GuardianClientConfig.builder()
                .maskinportenClientId(DUMMY_MASKINPORTEN_CLIENT_ID)
                .environment(GuardianClientConfig.Environment.PROD_BIP)
                .build();

        assertThat(config.getKeycloakUrl()).hasToString("https://keycloak.prod-bip-app.ssb.no");
        assertThat(config.getKeycloakTokenEndpoint()).isEqualTo ("/auth/realms/ssb/protocol/openid-connect/token");
    }

    @Test
    void deduceKeycloakUrl_bipProdWithCustomEndpoint_shouldUseCustomEndpoint() {
        GuardianClientConfig config = GuardianClientConfig.builder()
                .maskinportenClientId(DUMMY_MASKINPORTEN_CLIENT_ID)
                .environment(GuardianClientConfig.Environment.PROD_BIP)
                .keycloakTokenEndpoint("/foo/bar")
                .build();

        assertThat(config.getKeycloakUrl()).hasToString("https://keycloak.prod-bip-app.ssb.no");
        assertThat(config.getKeycloakTokenEndpoint()).isEqualTo ("/foo/bar");
    }

    @Test
    void guardianUrl_shouldThrowExceptionForMissingEnvironment() {
        GuardianClientConfig config = GuardianClientConfig.builder()
                .maskinportenClientId(DUMMY_MASKINPORTEN_CLIENT_ID)
                .build();

        assertThrows(IllegalStateException.class, config::getGuardianUrl);
    }

    @Test
    void shortenedTokenExpirationInSeconds_shouldReturnDefaultIfNotProvided() {
        GuardianClientConfig config = GuardianClientConfig.builder()
                .maskinportenClientId(DUMMY_MASKINPORTEN_CLIENT_ID)
                .build();

        assertThat(config.getShortenedTokenExpirationInSeconds()).isEqualTo(300);
    }

    @Test
    void keycloakTokenEndpoint_shouldReturnCustomEndpointIfProvided() {
        String customEndpoint = "/custom/endpoint";
        GuardianClientConfig config = GuardianClientConfig.builder()
                .maskinportenClientId(DUMMY_MASKINPORTEN_CLIENT_ID)
                .keycloakTokenEndpoint(customEndpoint)
                .build();

        assertThat(config.getKeycloakTokenEndpoint()).isEqualTo(customEndpoint);
    }

    @Test
    void keycloakUrl_shouldReturnCustomUrlIfProvided() {
        URI customUrl = URI.create("https://custom.keycloak.url");
        GuardianClientConfig config = GuardianClientConfig.builder()
                .maskinportenClientId(DUMMY_MASKINPORTEN_CLIENT_ID)
                .keycloakUrl(customUrl)
                .build();

        assertThat(config.getKeycloakUrl()).isEqualTo(customUrl);
    }

    @Test
    void guardianUrl_shouldReturnCustomUrlIfProvided() {
        URI customUrl = URI.create("https://custom.guardian.url");
        GuardianClientConfig config = GuardianClientConfig.builder()
                .maskinportenClientId(DUMMY_MASKINPORTEN_CLIENT_ID)
                .guardianUrl(customUrl)
                .build();

        assertThat(config.getGuardianUrl()).isEqualTo(customUrl);
    }

    @Test
    void getGuardianUrl_local_returnsCorrectUrl() {
        GuardianClientConfig config = GuardianClientConfig.builder()
                .maskinportenClientId(DUMMY_MASKINPORTEN_CLIENT_ID)
                .environment(GuardianClientConfig.Environment.LOCAL)
                .build();
        assertThat(config.getGuardianUrl()).hasToString("http://localhost:10310");
    }

    @Test
    void getGuardianUrl_prodBip_returnsCorrectUrl() {
        GuardianClientConfig config = GuardianClientConfig.builder()
                .maskinportenClientId(DUMMY_MASKINPORTEN_CLIENT_ID)
                .environment(GuardianClientConfig.Environment.PROD_BIP)
                .build();
        assertThat(config.getGuardianUrl()).hasToString("https://guardian.dapla.ssb.no");
    }

    @Test
    void getGuardianUrl_stagingBip_returnsCorrectUrl() {
        GuardianClientConfig config = GuardianClientConfig.builder()
                .maskinportenClientId(DUMMY_MASKINPORTEN_CLIENT_ID)
                .environment(GuardianClientConfig.Environment.STAGING_BIP)
                .build();
        assertThat(config.getGuardianUrl()).hasToString("https://guardian.dapla-staging.ssb.no");
    }
}