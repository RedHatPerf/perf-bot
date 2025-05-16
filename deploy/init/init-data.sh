#!/bin/bash

echo "Initializing Horreum data"

CWD="$(dirname "$0")"

API="${HORREUM_HOST}/api"

HORREUM_BOOTSTRAP_PASSWORD=${HORREUM_BOOTSTRAP_PASSWORD:-"secret"}
HORREUM_BOOTSTRAP_USER=${HORREUM_BOOTSTRAP_USER:-"horreum.bootstrap"}
HORREUM_TEST_NAME=${HORREUM_TEST_NAME:-"getting-started"}
HORREUM_SCHEMA_URI=${HORREUM_SCHEMA_URI:-"uri:getting-started:0.1"}

TOKEN=$(curl -s -X POST "$KEYCLOAK_HOST/realms/horreum/protocol/openid-connect/token" \
    -H 'content-type: application/x-www-form-urlencoded' \
    -d "username=${HORREUM_BOOTSTRAP_USER}&password=${HORREUM_BOOTSTRAP_PASSWORD}&grant_type=password&client_id=horreum-ui" \
    | jq -r .access_token)

echo "==== Getting API key ===="
API_KEY=$(curl -s -X POST "$API/user/apikey" -H "Authorization: Bearer $TOKEN" -H 'content-type: application/json' -d '{"name": "test-key", "type": "USER"}')

echo "Horreum API key is: $API_KEY"

echo "==== Creating schema ===="
echo ""
curl "$API/schema/import/" -X POST -H 'content-type: application/json' -H "X-Horreum-API-Key: $API_KEY" -d @"$CWD"/schema.json

echo "==== Creating tests ===="
echo ""
curl "$API/test/import/" -X POST -H 'content-type: application/json' -H "X-Horreum-API-Key: $API_KEY" -d @"$CWD"/test.json

echo "==== Uploading run ===="
echo ""
curl "$API/run/data?test=${HORREUM_TEST_NAME}&start=$.timestamps.start&stop=$.timestamps.stop&owner=dev-team&access=PUBLIC&schema=${HORREUM_SCHEMA_URI}" -X POST -H 'content-type: application/json' -H "X-Horreum-API-Key: $API_KEY" -d @"$CWD"/baseline_1.json
#curl $API"/run/data?test=${HORREUM_TEST_NAME}&start=$.timestamps.start&stop=$.timestamps.stop&owner=dev-team&access=PUBLIC&schema=${HORREUM_SCHEMA_URI}" -X POST -H 'content-type: application/json' -H "X-Horreum-API-Key: $API_KEY" -d @"$CWD"/assets/run_1.json

echo "Setting Jenkins credentials"

curl -X POST "http://${JENKINS_USER}:${JENKINS_KEY}@${JENKINS_HOST}/credentials/store/system/domain/_/createCredentials" --data-urlencode "json={
   \"\": \"0\",
   \"credentials\": {
     \"scope\": \"GLOBAL\",
     \"id\": \"horreum-apikey\",
     \"secret\": \"$API_KEY\",
     \"description\": \"Horreum API key for horreum.bootstrap user\",
     \"\$class\": \"org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl\"
   }
 }"