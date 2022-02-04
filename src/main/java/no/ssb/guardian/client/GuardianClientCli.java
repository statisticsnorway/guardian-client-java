package no.ssb.guardian.client;

import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.Set;

import static picocli.CommandLine.Help.Visibility.ALWAYS;

@Slf4j
@Command(name = "guardian",
        versionProvider = GuardianClientCli.class,
        description = "Retrieve access tokens from Maskinporten Guardian", mixinStandardHelpOptions = true)
public class GuardianClientCli implements Runnable, CommandLine.IVersionProvider {

    @Option(names = {"-v", "--verbose"},
            description = "Shows what's going on under the hood")
    boolean verbose;

    @Option(names = {"-e", "--env"}, showDefaultValue = ALWAYS,
            description = "Runtime environment (PROD, STAGING or LOCAL) the Guardian Client is working with")
    GuardianClientConfig.Environment environment = GuardianClientConfig.Environment.STAGING;

    @Option(names = {"-u", "--maskinporten-client-id"},
            description = "Maskinporten client ID", required = true)
    String maskinportenClientId;

    @Option(names = {"-s", "--scopes"}, split = ",",
            description = "Comma-separated list of maskinporten scopes (e.g. some:scope1,some:scope2)")
    Set<String> scopes;

    @CommandLine.ArgGroup(exclusive = true, multiplicity = "1")
    Exclusive exclusive;

    static class Exclusive {
        @Option(names = {"-p", "--keycloak-client-secret"},
                description = "Keycloak client secret (needed if Guardian client should fetch keycloak token)")
        String maskinportenClientSecret;

        @Option(names = {"-t", "--keycloak-token"},
                description = "Keycloak token")
        String staticKeycloakToken;
    }

    @Override
    public String[] getVersion() {
        return new String[] {String.format("%s (%s)",
                BuildInfo.INSTANCE.getVersion(),
                BuildInfo.INSTANCE.getBuildTimestamp())
        };
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new GuardianClientCli()).execute(args);
        System.exit(exitCode);
    }

    public void run() {
        final GuardianClient client;

        if (verbose) {
            // Used by the CmdLogbackFilter to decide if verbose logs should be printed to stderr
            System.setProperty("GUARDIAN_CLIENT_VERBOSE", "true");
        }

        // personal user
        if (exclusive.staticKeycloakToken != null) {
            client = new GuardianClient(GuardianClientConfig.builder()
                    .environment(environment)
                    .maskinportenClientId(maskinportenClientId)
                    .staticKeycloakToken(exclusive.staticKeycloakToken)
                    .build());
        }

        // service user
        else {
            client = new GuardianClient(GuardianClientConfig.builder()
                    .environment(environment)
                    .maskinportenClientId(maskinportenClientId)
                    .keycloakClientSecret(exclusive.maskinportenClientSecret.toCharArray())
                    .build());
        }

        String token = client.getMaskinportenAccessToken(scopes);


        // Print token to stdout
        System.out.println(token);
    }
}
