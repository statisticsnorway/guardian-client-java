package no.ssb.guardian.client;

import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertTrue;

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

}