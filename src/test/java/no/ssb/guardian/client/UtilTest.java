package no.ssb.guardian.client;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.Test;

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
        String jwtString = Jwts.builder().setSubject("user").compact();
        Claims claims = Util.jwtClaimsOf(jwtString);
        assertEquals("user", claims.getSubject());
    }

    @Test
    void jwtClaimsOf_shouldThrowExceptionForInvalidJwt() {
        String invalidJwt = "invalid.jwt.token";
        assertThrows(GuardianClientException.class, () -> Util.jwtClaimsOf(invalidJwt));
    }

    @Test
    void jwtExpirationDateOf_shouldReturnExpirationDate() {
        Date expiration = new Date(System.currentTimeMillis() + 10000);
        String jwtString = Jwts.builder().setExpiration(expiration).compact();
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