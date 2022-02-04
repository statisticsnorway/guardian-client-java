package no.ssb.guardian.client;

import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@RequiredArgsConstructor
@Value
public class AccessTokenWrapper {

    public AccessTokenWrapper(String id, String token) {
        this.id = id;
        this.expiresAt = Util.jwtExpirationDateOf(token).toInstant();
        this.token = token;
    }

    private final String id;
    private final String token;
    private final Instant expiresAt;

    public boolean isExpired() {
        return expiresAt.isAfter(Instant.now());
    }

    public long nanosUntilExpiration() {
        return Duration.between(Instant.now(), expiresAt).toNanos();
    }

    public long nanosUntilShortenedExpiration(long shortenedExpirationInSeconds) {
        return Duration
                .between(Instant.now(), expiresAt.minus(shortenedExpirationInSeconds, ChronoUnit.SECONDS))
                .toNanos();
    }

}
