#!/bin/bash

echo "Fetching auto generated keys"

HORREUM_BOOTSTRAP_USER=${HORREUM_BOOTSTRAP_USER:-"horreum.bootstrap"}
HORREUM_BOOTSTRAP_PWD=$(podman logs local.horreum-app | grep "Created temporary account '${HORREUM_BOOTSTRAP_USER}' with password" | awk '{print $NF}')
JENKINS_API_KEY=$(podman logs local.jenkins 2>&1 | grep "Generated Jenkins api key:" | awk '{print $NF}')

echo "HORREUM_BOOTSTRAP_PWD = ${HORREUM_BOOTSTRAP_PWD}"
echo "JENKINS_API_KEY = ${JENKINS_API_KEY}"