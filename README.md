
# Perf Bot 

This repository contains a GitHub App powered by [Quarkus GitHub App](https://github.com/quarkiverse/quarkus-github-app).

This Perf Bot GitHub App aims to make it easier to test performance earlier, detect regressions in 
development and CI, and reduce late-stage surprises or production issues.

## Usage

Check out the [use cases doc](docs/USE_CASES.md) for more information on the implemented interactions.

TODO: provide some usage examples

## Commands

TODO: list all available commands

## Local environment

For all the local environment credentials that are used to interact with the environment itself, 
please refer to the [credentials](#credentials) section.

### Prerequisites

* The perf bot is already installed as GitHub in whatever repository you want (lampajr/webhook-umb-example in this case)
* You have smee.io channel with which the GitHub app was configured

### Set Up

In the [deploy](deploy) folder there are some automation you can use to set up the local environment.

```bash
cd deploy && ./start.sh
```

After that, we can start up the Perf Bot:
```bash
mvn clean install -DskipTests -DskipITs -Dquarkus.container-image.build=true
podman run \
  --replace --name local.perf-bot --env-file ".env" \
  --network local_default \ 
  -v /tmp/jenkins-truststore.jks:/tmp/jenkins-truststore.jks:rw,Z \ 
  -e QUARKUS_GITHUB_APP_PRIVATE_KEY=$QUARKUS_GITHUB_APP_PRIVATE_KEY \
 -p 8081:8081 localhost/alampare/perf-bot:0.0.1-SNAPSHOT
```

Where `$QUARKUS_GITHUB_APP_PRIVATE_KEY` is the private key of the installed GitHub app and `.env` file contains
required GitHub app configuration, see https://docs.quarkiverse.io/quarkus-github-app/dev/create-github-app.html#_initialize_the_configuration
for more information.

If you want to override the Java configuration simply add 
`-e JAVA_OPTS_APPEND="-Dproxy.job.runner.jenkins.user=admin -Dproxy.job.runner.jenkins.apiKey=<JENKINS_API_KEY> -Dproxy.job.runner.jenkins.url=http://local.jenkins:8080 -Dproxy.datastore.horreum.url=http://local.horreum-app:8080"`

### Initialization

The initialization is performed as part of the local env set up, so no further manual actions are required at this
step.

The only thing to do is to update the [repo-config.json](scripts/assets/repo-config.json) horreumKey created as part of
the local env startup. Use `./scripts/fetch-keys.sh` to get it.

After that you can load the project/repo configuration in perf-bot by using:
```bash
./scripts/setup_config.sh
```

### Credentials

| Resource           |               Value                | Note                                              |
|--------------------|:----------------------------------:|---------------------------------------------------|
| Jenkins User       |               admin                |                                                   |
| Jenkins Pwd        |               secret               |                                                   |
| Jenkins API Key    | 11c513a545425a50c202367deefad6ed33 |                                                   |
| Horreum User       |         horreum.bootstrap          |                                                   |
| Horreum Pwd        |               secret               |                                                   |
| Horreum API Key    |       \<created at runtime\>       | This can be fetched using ./scripts/fetch-keys.sh |
| Keycloak Admin     |               admin                |                                                   |
| Keycloak Admin Pwd |               secret               |                                                   |