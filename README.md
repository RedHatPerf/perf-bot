
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

In the [scripts](scripts) folder there are some scripts that you can use to set up the local environment.

### Horreum set up
Assuming you already have Horreum and Jenkins up and running locally, you can run
```bash
HORREUM_BOOTSTRAP_PWD=<pwd> \
HORREUM_TEST_ID=10 \
API=http://localhost:18088/api \
./scripts/setup_horreum.sh
```

to populate the Horreum test with some simple configuration.

* `HORREUM_BOOTSTRAP_PWD`: password of the auto-generated `horreum.bootstrap` user in Horreum - default is `secret`
* `HORREUM_TEST_ID`: the id of the test, if already existing in Horreum - do not set this env if you want to create a new Test
* `API`: local Horreum API endpoint

### Jenkins set up
TODO: add jenkins setup

### Perf Bot set up

You can run it in dev mode with:
```bash
mvn \
  -Dproxy.job.runner.jenkins.user=<jenkins-user> \
  -Dproxy.job.runner.jenkins.apiKey=<jenkins-user-api-key> \
  -Dproxy.job.runner.jenkins.url="http://localhost:18080" \
  -Dproxy.datastore.horreum.url=http://localhost:18088 \
  quarkus:dev
```

Then you can load the repository configuration using `./scripts/config/setup_config.sh`