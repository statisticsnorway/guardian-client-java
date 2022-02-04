package no.ssb.guardian.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.*;
import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@UtilityClass
public class Util {

    private static final JwtParser JWT_PARSER = Jwts.parserBuilder().build();
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
     * jwtClaimsOf parses a JWT and returns its Claims
     */
    public static Claims jwtClaimsOf(String jwtString) {
        try {
            String withoutSignature = jwtString.substring(0, jwtString.lastIndexOf('.') + 1);
            Jwt<Header, Claims> jwt = JWT_PARSER.parseClaimsJwt(withoutSignature);
            return jwt.getBody();
        }
        catch (Exception e) {
            throw new GuardianClientException("Error parsing claims from jwt: " + jwtString, e);
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
