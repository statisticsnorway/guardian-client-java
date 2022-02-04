package no.ssb.guardian.client;

public class GuardianClientException  extends RuntimeException {

    public GuardianClientException(String message) {
        super(message);
    }

    public GuardianClientException(String message, Throwable cause) {
        super(message, cause);
    }

}
