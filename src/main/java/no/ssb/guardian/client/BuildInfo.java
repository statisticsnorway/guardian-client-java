package no.ssb.guardian.client;

public enum BuildInfo {

    INSTANCE;

    /**
     * @return the project version such as 1.1-SNAPSHOT.
     */
    public String getVersion() {
        return "unknown";
    }

    /**
     * @return the build timestamp such as 2022-02-03T19:45:27Z.
     */
    public String getBuildTimestamp() {
        return "unknown";
    }
}
