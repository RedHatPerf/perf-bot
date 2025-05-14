
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

### Prerequisites

* The perf bot is already installed as GitHub in whatever repository you want (lampajr/webhook-umb-example in this case)
* You have smee.io channel with which the GitHub app was configured

### Set Up

In the [deploy](deploy) folder there are some automation you can use to set up the local environment.

```bash
cd deploy && ./start.sh
```

Then, once Horreum is up and running, in a different terminal issue:

```bash
$ ./deploy/fetch-keys.sh

Fetching auto generated keys
HORREUM_BOOTSTRAP_PWD = <BLA BLA BLA>
JENKINS_API_KEY = <BLA BLA BLA>
```

After that, we can start up the Perf Bot:
```bash
mvn clean install -DskipTests -DskipITs -Dquarkus.container-image.build=true
podman run \
  --replace --name local.perf-bot --env-file ".env" \
  --network local_default \ 
  -v /tmp/jenkins-truststore.jks:/tmp/jenkins-truststore.jks:rw,Z \ 
  -e QUARKUS_GITHUB_APP_PRIVATE_KEY=$QUARKUS_GITHUB_APP_PRIVATE_KEY \
  -e JAVA_OPTS_APPEND="-Dproxy.job.runner.jenkins.user=admin -Dproxy.job.runner.jenkins.apiKey=<JENKINS_API_KEY> -Dproxy.job.runner.jenkins.url=http://local.jenkins:8080 -Dproxy.datastore.horreum.url=http://local.horreum-app:8080" \
 -p 8081:8081 localhost/alampare/perf-bot:0.0.1-SNAPSHOT
```

Where `$QUARKUS_GITHUB_APP_PRIVATE_KEY` is the private key of the installed GitHub app and `.env` file contains
required GitHub app configuration, see https://docs.quarkiverse.io/quarkus-github-app/dev/create-github-app.html#_initialize_the_configuration
for more information.

Replace `<JENKINS_API_KEY>` with the value obtained during the previous step.

### Initialization

Initialize Horreum by creating Schema, Test and a simple baseline run.

```bash
HORREUM_BOOTSTRAP_PWD=<HORREUM_BOOTSTRAP_PWD> API=http://localhost:18088/api ./scripts/horreum/setup_horreum.sh
```

Replace `<HORREUM_BOOTSTRAP_PWD>`with the value obtained from `fetch-keys.sh` script.

The output should be something like:
```bash
==== Getting API key ====
You API key is: HUSR_F41DEDE9_E2AC_44CE_A99E_3F9B7460C3B2
==== Creating schema ====

1==== Creating tests ====

10==== Uploading run ====

1%      
```

Create the horreum api key credential in Jenkins:
* Id: `horreum-apikey`
* Secret: `HUSR_F41DEDE9_E2AC_44CE_A99E_3F9B7460C3B2` (this will change everytime)

Update the [repo-config.json](scripts/config/assets/repo-config.json) horreumKey with the same value obtained 
in the previous step and load the configuration in the perf bot:

```bash
./scripts/config/setup_config.sh
```
