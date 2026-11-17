package no.ssb.guardian.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

@UtilityClass
public class Util {

    private static final ObjectMapper OBJECT_MAPPER;

    static {
        OBJECT_MAPPER = new ObjectMapper();
        OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    /**
     * base64EncodedCredentials base64 encodes a clientId/clientSecret pair such that they can be used as a
     * basic Authorization header (Authorization: Basic [value]).
     */
    public static String base64EncodedCredentials(String clientId, char[] clientSecret) {
        byte[] payload = String.format("%s:%s", clientId, String.valueOf(clientSecret)).getBytes(StandardCharsets.UTF_8);
        return Base64.getEncoder().encodeToString(payload);
    }

    /**
     * jsonToMap converts a JSON to String->Object map
     */
    public static Map<String, Object> jsonToMap(String json) {
        try {
            return OBJECT_MAPPER.readValue(json, new TypeReference<Map<String, Object>>() {});
        }
        catch (IOException e) {
            throw new GuardianClientException("Error mapping JSON to map", e);
        }
    }

    /**
     * toJson converts any Object to JSON
     */
    public static String toJson(Object object) {
        try {
            return OBJECT_MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new GuardianClientException("Error mapping " +  object.getClass().getSimpleName() + " object to JSON", e);
        }
    }

    /**
     * jwtClaimsOf decodes the payload of a JWT and returns its Claims.
     * <p>
     * The signature is NOT verified: this is only used to read claims (e.g. expiry) from tokens we received
     * directly from the token issuer. The payload is decoded directly instead of via jjwt's unsecured parser,
     * which (since jjwt 0.12) rejects signed tokens (e.g. alg RS256) once the signature is stripped.
     */
    public static Claims jwtClaimsOf(String jwtString) {
        try {
            String[] parts = jwtString.split("\\.", -1);
            if (parts.length < 2 || parts[1].isEmpty()) {
                throw new IllegalArgumentException("JWT has no payload part");
            }
            byte[] payload = Base64.getUrlDecoder().decode(parts[1]);
            Map<String, Object> claims = OBJECT_MAPPER.readValue(payload, new TypeReference<Map<String, Object>>() {});
            return Jwts.claims().add(claims).build();
        }
        catch (Exception e) {
            // Never include the token itself in the message: it is a live credential and ends up in logs.
            throw new GuardianClientException("Error parsing claims from jwt", e);
        }
    }

    /**
     * jwtExpirationDateOf returns the expiration date of a JWT
     */
    public static Date jwtExpirationDateOf(String jwtString) {
        return jwtClaimsOf(jwtString).getExpiration();
    }

    /**
     * jwtClaimOf optionally returns a named claim searched for in the given JWT
     */
    public static Optional<Object> jwtClaimOf(String jwtString, String claimName) {
        return Optional.ofNullable(jwtClaimsOf(jwtString).get(claimName));
    }

}
