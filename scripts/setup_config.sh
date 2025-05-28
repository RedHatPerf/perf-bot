#!/usr/bin/bash

set -e

CWD="$(dirname "$0")"

# Usage: REPO_FULL_NAME=lampajr/webhook-example ./scripts/setup_config.sh
API=${API:-"http://localhost:8081/"}

REPO_FULL_NAME=${REPO_FULL_NAME:-"lampajr/webhook-umb-example"}
DATASTORE_API_KEY=${DATASTORE_API_KEY:-"CHANGE_ME"}
PLATFORM_API_KEY=${PLATFORM_API_KEY:-"CHANGE_ME"}

ESCAPED_REPO_FULL_NAME=$(printf '%s\n' "$REPO_FULL_NAME" | sed 's/[\/&]/\\&/g')
CONFIG_CONTENT=$(cat "$CWD/assets/repo-config.json")

if [ "$DATASTORE_API_KEY" = "CHANGE_ME" ]; then
  echo "Missing datastore api key, trying to fetch from local.horreum-init"
  RAW_KEY=$(podman logs local.horreum-init 2>&1 | grep "Horreum API key is: " | awk '{print $7}')
  DATASTORE_API_KEY=$(echo "$RAW_KEY" | tr -d '\r' | tr -d '\n') # trims returns and newlines
  # trims leading/trailing spaces
  DATASTORE_API_KEY=$(echo "$DATASTORE_API_KEY" | sed -e 's/^[[:space:]]*//' -e 's/[[:space:]]*$//')
  DATASTORE_API_KEY=$(echo "$DATASTORE_API_KEY" | sed 's/^"//;s/"$//') # Remove leading and trailing double quotes
  DATASTORE_API_KEY=$(echo "$DATASTORE_API_KEY" | sed "s/^'//;s/'$//") # Remove leading and trailing single quotes
  echo "Using datastoreApiKey: $DATASTORE_API_KEY"
fi

if [ "$PLATFORM_API_KEY" = "CHANGE_ME" ]; then
  echo "Missing platform api key, trying to fetch from local.jenkins"
  RAW_KEY=$(podman logs local.jenkins 2>&1 | grep "Setting up static Jenkins api key: " | awk '{print $NF}')
  PLATFORM_API_KEY=$(echo "$RAW_KEY" | tr -d '\r' | tr -d '\n') # trims returns and newlines
  # trims leading/trailing spaces
  PLATFORM_API_KEY=$(echo "$PLATFORM_API_KEY" | sed -e 's/^[[:space:]]*//' -e 's/[[:space:]]*$//')
  PLATFORM_API_KEY=$(echo "$PLATFORM_API_KEY" | sed 's/^"//;s/"$//') # Remove leading and trailing double quotes
  PLATFORM_API_KEY=$(echo "$PLATFORM_API_KEY" | sed "s/^'//;s/'$//") # Remove leading and trailing single quotes
  echo "Using jobPlatformApiKey: $PLATFORM_API_KEY"
fi

MODIFIED_CONFIG_CONTENT=$(echo "$CONFIG_CONTENT" | sed "s|\(\"repoFullName\": \"\)[^\"]*\"|\1${ESCAPED_REPO_FULL_NAME}\"|")
MODIFIED_CONFIG_CONTENT=$(echo "$MODIFIED_CONFIG_CONTENT" | sed "s|\(\"repositoryUrl\": \"\)[^\"]*\"|\1https://github.com/${ESCAPED_REPO_FULL_NAME}\"|")
MODIFIED_CONFIG_CONTENT=$(echo "$MODIFIED_CONFIG_CONTENT" | sed "s|\(\"datastoreApiKey\": \"\)[^\"]*\"|\1${DATASTORE_API_KEY}\"|")
MODIFIED_CONFIG_CONTENT=$(echo "$MODIFIED_CONFIG_CONTENT" | sed "s|\(\"jobPlatformApiKey\": \"\)[^\"]*\"|\1${PLATFORM_API_KEY}\"|")

echo "==== Loading config ===="
echo ""
echo "Config: $MODIFIED_CONFIG_CONTENT"
curl "$API/config" -X POST -H 'content-type: application/json' -d "$MODIFIED_CONFIG_CONTENT"
echo ""
echo "Config loaded!"
