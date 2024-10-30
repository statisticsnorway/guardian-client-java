package no.ssb.guardian.client;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

/**
 *       ,//)
 *     ,;;' \
 *   ,;;' ( '\
 *       / '\_)  The workhorse
 */
@Slf4j
public class GuardianClient {

    static final Marker VERBOSE = MarkerFactory.getMarker("VERBOSE");
    static final String GUARDIAN_CLIENT_VERBOSE_PROPERTY = "GUARDIAN_CLIENT_VERBOSE";

    private final GuardianClientConfig config;
    private final Cache<String, AccessTokenWrapper> cache;
    private final KeycloakTokenResolver keycloakTokenResolver;
    private final MaskinportenTokenResolver maskinportenTokenResolver;

    public GuardianClient(GuardianClientConfig config) {
        this(config, new DefaultKeycloakTokenResolver(config), new DefaultMaskinportenTokenResolver(config));
    }

    GuardianClient(@NonNull GuardianClientConfig config,
                   @NonNull KeycloakTokenResolver keycloakTokenResolver,
                   @NonNull MaskinportenTokenResolver maskinportenTokenResolver) {
        this.config = config;
        this.keycloakTokenResolver = keycloakTokenResolver;
        this.maskinportenTokenResolver = maskinportenTokenResolver;
        this.cache = Caffeine.newBuilder()
                .expireAfter(new Expiry<String, AccessTokenWrapper>() {
                    @Override
                    public long expireAfterCreate(String key, AccessTokenWrapper token, long currentTime) {
                        return token.nanosUntilShortenedExpiration(config.getShortenedTokenExpirationInSeconds());
                    }
                    @Override
                    public long expireAfterUpdate(String key, AccessTokenWrapper token, long currentTime, long currentDuration) {
                        return currentDuration;
                    }
                    @Override
                    public long expireAfterRead(String key, AccessTokenWrapper token, long currentTime, long currentDuration) {
                        return currentDuration;
                    }
                })
                .build();
        log.debug("GuardianClient initialized with config: {}", config.toDebugString());
    }

    /**
     * withConfig provides a fluent way to initialize a GuardianClient, like so:
     * <pre>
     *     GuardianClient.withConfig()
     *     .environment(TEST)
     *     .maskinportenClientId("some-uuid")
     *     .keycloakClientSecret("some-secret")
     *     .create();
     * </pre>
     *
     * @return a GuardianClientConfig builder
     */
    public static GuardianClientConfig.GuardianClientConfigBuilder withConfig() {
        return GuardianClientConfig.builder();
    }

    /**
     * Retrieve maskinporten access token from Maskinporten Guardian
     *
     * @return access token that can be used to access APIs protected by maskinporten
     */
    public String getMaskinportenAccessToken() {
        return getMaskinportenAccessToken(Collections.emptySet());
    }

    /**
     * Retrieve maskinporten access token from Maskinporten Guardian
     *
     * @param scopes maskinporten scopes
     * @return access token that can be used to access APIs protected by maskinporten
     */
    public String getMaskinportenAccessToken(String... scopes) {
        return getMaskinportenAccessToken(Set.of(scopes));
    }

    /**
     * Retrieve maskinporten access token from Maskinporten Guardian
     *
     * @param scopes maskinporten scopes
     * @return access token that can be used to access APIs protected by maskinporten
     */
    public String getMaskinportenAccessToken(Set<String> scopes) {
        String keycloakToken = getKeycloakAccessToken();
        return cache.get(config.getMaskinportenClientId(),
                id -> maskinportenTokenResolver.getMaskinportenAccessToken(
                        keycloakToken,
                        Optional.ofNullable(scopes).orElse(Collections.emptySet()))
        ).getToken();
    }

    private String getKeycloakAccessToken() {
        if (config.getStaticKeycloakToken() != null) {
            log.debug(VERBOSE, "Using static keycloak token");
            log.trace(VERBOSE, config.getStaticKeycloakToken());
            return config.getStaticKeycloakToken();
        }

        return cache.get(config.getKeycloakClientId(), id -> keycloakTokenResolver.getKeycloakAccessToken())
                .getToken();
    }

    public static String userAgent() {
        return String.format("guardian-client-java/%s", BuildInfo.INSTANCE.getVersion());
    }

}
