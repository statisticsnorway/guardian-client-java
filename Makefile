# Uncomment if running the CLI commands
include .env

.PHONY: default
default: | help

.PHONY: build-mvn
build-mvn: ## Build project and install to you local maven repo
	mvn clean install

.PHONY: run-cli-help
run-cli-help: ## Run CLI --help
	@GUARDIAN_CLIENT_JAR=target/guardian-client-*.jar bin/guardian-client --help

.PHONY: run-cli-as-service-user
run-cli-as-service-user: ## Run the CLI as a service user
	@GUARDIAN_CLIENT_JAR=target/guardian-client-*.jar bin/guardian-client \
	--env $(TEST_ENVIRONMENT) \
	--maskinporten-client-id $(TEST_MASKINPORTEN_CLIENT_ID) \
	--keycloak-client-secret $(TEST_KEYCLOAK_CLIENT_SECRET) \
	--scopes $(TEST_MASKINPORTEN_SCOPES)

.PHONY: run-cli-with-static-keycloak-token
run-cli-with-static-keycloak-token: ## Run the CLI with an explicit keycloak token (can be used for personal users)
	@GUARDIAN_CLIENT_JAR=target/guardian-client-*.jar bin/guardian-client \
	--env $(TEST_ENVIRONMENT) \
	--maskinporten-client-id $(TEST_MASKINPORTEN_CLIENT_ID) \
	--keycloak-token $(TEST_KEYCLOAK_TOKEN) \
	--scopes $(TEST_MASKINPORTEN_SCOPES)

.PHONY: release-dryrun
release-dryrun: ## Simulate a release in order to detect any issues
	mvn release:prepare release:perform -Darguments="-Dmaven.deploy.skip=true" -DdryRun=true

.PHONY: release
release: ## Release a new version. Update POMs and tag the new version in git
	git push origin master:release

.PHONY: help
help:
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'
