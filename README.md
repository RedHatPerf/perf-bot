
# Perf Bot 

This repository contains a GitHub App powered by [Quarkus GitHub App](https://github.com/quarkiverse/quarkus-github-app).

This Perf Bot GitHub App aims to make it easier to test performance earlier, detect regressions in 
development and CI, and reduce late-stage surprises or production issues.

> [!WARNING]
> This project is currently under development, and therefore subject to change in the future.

## About

Check out the [architecture docs](docs/architecture/index.md) for more information on the Perf Bot internals and 
the supported workflows.

## Providers

Currently, the `Perf Bot` supports 2 different source of events:

* `GitHubEventHandler`, the default [GitHub Handler](src/main/java/io/perf/tools/bot/handler/event/GitHubEventHandler.java)
   as defined by the [Quarkus GitHub Framework](https://docs.quarkiverse.io/quarkus-github-app/dev/create-github-app.html).
* `AmqpEventHandler`, a custom [AMQP handler](src/main/java/io/perf/tools/bot/handler/event/AmqpEventHandler.java) that
   consumes events from a configured AMQP channel.

Required configuration:

```dotenv
# GitHub app configuration
QUARKUS_GITHUB_APP_APP_ID=<the numeric app id>
QUARKUS_GITHUB_APP_APP_NAME=<the name of your app>
QUARKUS_GITHUB_APP_WEBHOOK_PROXY_URL=<your Smee.io channel URL>
QUARKUS_GITHUB_APP_WEBHOOK_SECRET=<your webhook secret>
QUARKUS_GITHUB_APP_PRIVATE_KEY=-----BEGIN RSA PRIVATE KEY-----\
                  <your private key>                          \
-----END RSA PRIVATE KEY-----

# AMQP configuration
AMQP_HOST=<amqp host>
AMQP_PORT=5671
AMQP_USERNAME=<amqp username>
AMQP_PASSWORD=<amqp password>
```

> [!NOTE]
> If you are running in production or using a Webhook proxy other than smee.io, you don't
> need `QUARKUS_GITHUB_APP_WEBHOOK_PROXY_URL` env variable.

## Commands

Here a list of the supported commands:

TODO

## AMQP Bridge

If you're operating within a private network and cannot receive webhook events directly from GitHub, you may want to 
set up an intermediate *bridge* to fetch those events and forward them to a message broker such as ActiveMQ.

To support this scenario, we've implemented a handler that consumes GitHub events from a pre-configured queue 
(see [providers](#providers) section).

You'll need to provide the following configuration:

- `AMQP_HOST`: Hostname of the AMQP broker
- `AMQP_PORT`: Port of the AMQP broker
- `AMQP_USERNAME`: Service account username for the Perf Bot
- `AMQP_PASSWORD`: Service account password for the Perf Bot

If running with `prod` profile, you may also need service account certificates, saved in the following paths:

- `/tmp/certs/service-account.key`
- `/tmp/certs/service-account.crt`
- `/tmp/certs/ca.crt`

> [!NOTE] 
> The private key is likely encrypted with a password, which Vert.x does not currently support.  
> To work around this, you can decrypt the key using the following command:
>```bash
>openssl rsa -in service-account.key -out service-account-unencrypted.key
>```

## Local environment

For all the local environment credentials that are used to interact with the environment itself, 
please refer to the [credentials](#credentials) section.

> [!IMPORTANT]  
> The local environment setup has been automated as part of quarkus development startup, i.e., running 
> `mvn quarkus:dev` will start up the local env automatically. You can disable this behavior by adding 
> `-Dquarkus.compose.devservices.enabled=false`


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
  -v /tmp/certs/:/tmp/certs/:rw,Z \
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

