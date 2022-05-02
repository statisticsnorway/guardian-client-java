package no.ssb.guardian.client;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GuardianClientConfigTest {
    private static final String DUMMY_MASKINPORTEN_CLIENT_ID = "2fed8b0e-2d18-4eb9-81b2-fee27448518d";

    @Test
    void deduceGuardianUrl_staging_shouldUseExternalUrl() {
        GuardianClientConfig config = GuardianClientConfig.builder()
                .maskinportenClientId(DUMMY_MASKINPORTEN_CLIENT_ID)
                .environment(GuardianClientConfig.Environment.STAGING)
                .build();

        assertThat(config.getGuardianUrl()).hasToString("https://guardian.dapla-staging.ssb.no");
    }

    @Test
    void deduceGuardianUrl_internalAccess_shouldUseInternalUrl() {
        GuardianClientConfig config = GuardianClientConfig.builder()
                .maskinportenClientId(DUMMY_MASKINPORTEN_CLIENT_ID)
                .environment(GuardianClientConfig.Environment.STAGING)
                .internalAccess(true)
                .build();

        assertThat(config.getGuardianUrl()).hasToString("http://maskinporten-guardian.dapla.svc.cluster.local");
    }

}