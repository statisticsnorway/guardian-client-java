# Guardian Client

Java client library for retrieving access tokens from [maskinporten-guardian](https://github.com/statisticsnorway/maskinporten-guardian).
It also provides a CLI if you want to use it from the command line.

[Maskinporten Guardian](https://github.com/statisticsnorway/maskinporten-guardian) allows SSB services and
trusted users to retrieve Maskinporten access tokens. It relies on keycloak to authorize the
users.

## Why?

* _YOLO_ - No need to learn about the Maskinporten Guardian API. Just retrieve them tokens and get on with your life.
* The Guardian Client maintains a local cache of keycloak and maskinporten tokens and provides a means for
  automatically re-fetching tokens only when they need to be retrieved. Reduces chattiness and
  unnecessary network traffic.
* Keeps track of default service locations - just let `guardian-client` know which environment you're running within.
* Use either as a library or from the command line
* No transitive dependencies - `guardian-client` only requires the JDK (11).
* You're lazy. Lazy is good :)

When accessing Maskinporten Guardian as a service user (the preferred way), `guardian-client` will
automatically retrieve and refresh the required keycloak access token if you configure the keycloak client secret.

## Getting started

#### Maven
```xml
<dependency>
    <groupId>no.ssb.guardian</groupId>
    <artifactId>guardian-client</artifactId>
    <version>[version]</version>
    <scope>compile</scope>
</dependency>
```
#### Gradle
```
implementation 'no.ssb.guardian:guardian-client:[version]'
```

## Usage

Instantiate the Guardian client:

```java
    GuardianClient client = GuardianClient.withConfig()
    .environment(STAGING)
    .maskinportenClientId("some-uuid")
    .keycloakClientSecret("some-secret")
    .create();
```

Retrieve maskinporten token:

```java
    String accessToken = client.getMaskinportenAccessToken("some:scope1", "some:scope2");
```

## Configuration options

| Key                               | Description                                                                                                                                                                                        |                                        Default |
|-----------------------------------|:---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------:|
| maskinportenClientId              | Maskinporten client ID from samhandlingsportalen - typically a UUID.                                                                                                                               |                                              - |
| environment                       | Optionally denotes the runtime environment (`PROD`, `STAGING` or `LOCAL`) the Guardian Client is working with. By specifying this you can rely on default values for guardianUrl and keycloakUrl   |                                              - |
| guardianUrl                       | Root URL of the maskinporten-guardian server (not needed if you specify `environment`)                                                                                                             |                                              - |
| keycloakUrl                       | Root URL of the keycloak server (not needed if you specify `environment`)                                                                                                                          |                                              - |
| keycloakTokenEndpoint             | Path to the token endpoint of the keycloak server. Only required if you're using a non-standard endpoint                                                                                           | /auth/realms/ssb/protocol/openid-connect/token |
| keycloakClientId                  | Keycloak client credentials id for the keycloak service user. *You will typically want to rely on the default value for this.*                                                                     |        maskinporten-`<maskinporten client id>` |
| keycloakClientSecret              | Keycloak client credentials secret for the maskinporten keycloak service user. Only needed for service users.                                                                                      |                                              - |
| staticKeycloakToken               | Configure if you want to explicitly use a specific keycloak token. Note that you are responsible for making sure the provided keycloak token is valid (not expired) and refreshed whenever needed. |                                              - |
| shortenedTokenExpirationInSeconds | The number of seconds actual token expiration time that the caches will use to determine whether or not a token needs to be refetched.                                                             |                                300 (5 minutes) |



## Using the Guardian Client CLI

In addition to being a java client library, the Guardian Client can be used from the command line.

```sh
$ guardian-client --help

Usage: guardian-client [-hvV] [-e=<environment>] -u=<maskinportenClientId>
                       [-s=<scopes>[,<scopes>...]]...
                       (-p=<maskinportenClientSecret> |
                       -t=<staticKeycloakToken>)
Retrieve access tokens from Maskinporten Guardian
  -e, --env=<environment>   Runtime environment (PROD, STAGING or LOCAL) the
                              Guardian Client is working with
                              Default: STAGING
  -h, --help                Show this help message and exit.
  -p, --keycloak-client-secret=<maskinportenClientSecret>
                            Keycloak client secret (needed if Guardian client
                              should fetch keycloak token)
  -s, --scopes=<scopes>[,<scopes>...]
                            Comma-separated list of maskinporten scopes (e.g.
                              some:scope1,some:scope2)
  -t, --keycloak-token=<staticKeycloakToken>
                            Keycloak token
  -u, --maskinporten-client-id=<maskinportenClientId>
                            Maskinporten client ID
  -v, --verbose             Shows what's going on under the hood
  -V, --version             Print version information and exit.
```

We recommend that you invoke the CLI application using the [guardian-client shell wrapper](bin/guardian-client).
Also, make sure to configure the `GUARDIAN_CLIENT_JAR` environment variable, pointing at the
guardian-client jar file.

### Install the Guardian Client CLI locally

If you want to use the Guardian Client CLI from "anywhere":
* The [guardian-client shell wrapper](bin/guardian-client) must be on your path
* Set the `GUARDIAN_CLIENT_JAR` environment variable to point at the guardian-client jar file

*Example config (.bashrc/.zsh):*
```
export GUARDIAN_CLIENT_HOME=path/to/guardian-client-java
export GUARDIAN_CLIENT_JAR=$GUARDIAN_CLIENT_HOME/target/guardian-client-*.jar
export PATH=:$GUARDIAN_CLIENT_HOME/bin:$PATH
```

### Example: Run the CLI as a service user
```sh
$ guardian-client --verbose \
   --env STAGING \
   --maskinporten-client-id <maskinporten client id> \
   --keycloak-client-secret <keycloak client secret>  \
   --scopes some:scope1,some:scope2
```

### Example: Run the CLI with static (e.g. personal) keycloak token
```sh
$ guardian-client --verbose \
   --env STAGING \
   --maskinporten-client-id <maskinporten client id> \
   --keycloak-token <ey...> \
   --scopes some:scope1,some:scope2
```

### Run CLI examples using `make`

The above examples can be run using _Make_. Create a `.env` file with your
settings (see the `.env-example` file). Then:

```sh
make run-cli-as-service-user
```

or

```sh
make run-cli-with-static-keycloak-token
```

## Development

Use `make` for common tasks:

```
build-mvn                            Build project and install to you local maven repo
run-cli-help                         Run CLI --help
run-cli-as-service-user              Run the CLI as a service user
run-cli-with-static-keycloak-token   Run the CLI with an explicit keycloak token (can be used for personal users)
release-dryrun                       Simulate a release to detect any issues
release                              Release a new version. Update POMs and tag the new version in git
```

## Prerequisites

JDK 11
