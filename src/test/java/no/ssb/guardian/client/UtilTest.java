package no.ssb.guardian.client;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UtilTest {

    @Test
    void base64EncodedCredentials() {
        String clientId = "user";
        String clientSecret = "pass";
        String b64Encoded = Util.base64EncodedCredentials(clientId, clientSecret.toCharArray());
        String decoded = new String(Base64.getDecoder().decode(b64Encoded));
        assertTrue(decoded.startsWith(clientId));
        assertTrue(decoded.endsWith(clientSecret));
    }


    @Test
    void jsonToMap_shouldConvertJsonToMap() {
        String json = "{\"key\":\"value\"}";
        Map<String, Object> map = Util.jsonToMap(json);
        assertEquals("value", map.get("key"));
    }

    @Test
    void jsonToMap_shouldThrowExceptionForInvalidJson() {
        String invalidJson = "{key:value}";
        assertThrows(GuardianClientException.class, () -> Util.jsonToMap(invalidJson));
    }

    @Test
    void toJson_shouldConvertObjectToJson() {
        Map<String, String> map = Map.of("key", "value");
        String json = Util.toJson(map);
        assertTrue(json.contains("\"key\":\"value\""));
    }

    @Test
    void toJson_shouldThrowExceptionForInvalidObject() {
        Object invalidObject = new Object() {
            @Override
            public String toString() {
                throw new RuntimeException("Invalid object");
            }
        };
        assertThrows(GuardianClientException.class, () -> Util.toJson(invalidObject));
    }

    @Test
    void jwtClaimsOf_shouldReturnClaims() {
        String jwtString = Jwts.builder().subject("user").compact();
        Claims claims = Util.jwtClaimsOf(jwtString);
        assertEquals("user", claims.getSubject());
    }

    /**
     * Real tokens from Keycloak/Maskinporten are signed (alg RS256). jjwt 0.12's unsecured parser rejects
     * such a token once the signature is stripped, so this must work without going through that parser.
     */
    @Test
    void jwtClaimsOf_shouldReturnClaimsForSignedRs256Jwt() {
        KeyPair keyPair = Jwts.SIG.RS256.keyPair().build();
        Date expiration = new Date(System.currentTimeMillis() + 60_000);
        String jwtString = Jwts.builder()
                .subject("user")
                .expiration(expiration)
                .claim("key", "value")
                .signWith(keyPair.getPrivate(), Jwts.SIG.RS256)
                .compact();

        Claims claims = Util.jwtClaimsOf(jwtString);

        assertEquals("user", claims.getSubject());
        assertEquals("value", claims.get("key"));
        assertEquals(expiration.getTime() / 1000, Util.jwtExpirationDateOf(jwtString).getTime() / 1000);
    }

    @Test
    void jwtClaimsOf_shouldThrowExceptionForJwtWithoutPayload() {
        assertThrows(GuardianClientException.class, () -> Util.jwtClaimsOf("no-dots-here"));
    }

    @Test
    void jwtClaimsOf_shouldThrowExceptionForInvalidJwt() {
        String invalidJwt = "invalid.jwt.token";
        assertThrows(GuardianClientException.class, () -> Util.jwtClaimsOf(invalidJwt));
    }

    @Test
    void jwtExpirationDateOf_shouldReturnExpirationDate() {
        Date expiration = new Date(System.currentTimeMillis() + 10000);
        String jwtString = Jwts.builder().expiration(expiration).compact();
        Date result = Util.jwtExpirationDateOf(jwtString);

        assertEquals(expiration.getTime() / 1000, result.getTime() / 1000);
    }

    @Test
    void jwtClaimOf_shouldReturnClaimIfExists() {
        String jwtString = Jwts.builder()
                .claim("key", "value")
                .compact();
        Optional<Object> claim = Util.jwtClaimOf(jwtString, "key");
        assertTrue(claim.isPresent());
        assertEquals("value", claim.get());
    }

}