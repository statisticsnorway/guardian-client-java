package no.ssb.guardian.client;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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

}