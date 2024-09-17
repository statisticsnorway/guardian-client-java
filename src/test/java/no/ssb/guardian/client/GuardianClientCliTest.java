package no.ssb.guardian.client;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class GuardianClientCliTest {
    private ByteArrayOutputStream outContent;
    private GuardianClient mockGuardianClient;


    final PrintStream originalOut = System.out;
    final PrintStream originalErr = System.err;
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    final ByteArrayOutputStream err = new ByteArrayOutputStream();

    @BeforeEach
    public void setUpStreams() {
        out.reset();
        err.reset();
        System.setOut(new PrintStream(out));
        System.setErr(new PrintStream(err));
        mockGuardianClient = mock(GuardianClient.class);
    }

    @AfterEach
    public void restoreStreams() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    int executeClient(String[] args) {
        return new CommandLine(new GuardianClientCli(mockGuardianClient)).execute(args);
    }

    @Test
    void verboseOption_shouldSetSystemProperty() {
        String[] args = {"-v", "-u", "maskinporten-client-id", "-p", "secret"};
        executeClient(args);
        assertThat(System.getProperty("GUARDIAN_CLIENT_VERBOSE")).isEqualTo("true");
    }

    @Test
    void missingRequiredOption_shouldShowUsageMessage() throws Exception {
        String[] args = {"-v"};
        int exitCode = executeClient(args);
        assertThat(exitCode).isNotEqualTo(0);
        assertThat(err.toString()).contains("Usage: guardian");
    }
}