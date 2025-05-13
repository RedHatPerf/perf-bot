#!/usr/bin/bash


# Using local env: HORREUM_BOOTSTRAP_PWD=<pwd> HORREUM_TEST_ID=10 API=http://localhost:18088/api ./scripts/setup_horreum.sh
CWD="$(dirname "$0")"

API=${API:-"http://localhost:8080/api"}

HORREUM_BOOTSTRAP_PWD=${HORREUM_BOOTSTRAP_PWD:-"secret"}
HORREUM_BOOTSTRAP_USER=${HORREUM_BOOTSTRAP_USER:-"horreum.bootstrap"}
HORREUM_TEST_ID=${HORREUM_TEST_ID:-""}
HORREUM_TEST_NAME=${HORREUM_TEST_NAME:-"getting-started"}
HORREUM_SCHEMA_URI=${HORREUM_SCHEMA_URI:-"uri:getting-started:0.1"}

KEYCLOAK_URL=$( curl -k -s $API/config/keycloak | jq -r '.url')
TOKEN=$(curl -s -X POST "$KEYCLOAK_URL"/realms/horreum/protocol/openid-connect/token \
    -H 'content-type: application/x-www-form-urlencoded' \
    -d "username=${HORREUM_BOOTSTRAP_USER}&password=${HORREUM_BOOTSTRAP_PWD}&grant_type=password&client_id=horreum-ui" \
    | jq -r .access_token)

echo "==== Getting API key ===="
API_KEY=$(curl -s -X POST $API'/user/apikey' -H "Authorization: Bearer $TOKEN" -H 'content-type: application/json' -d '{"name": "test-key", "type": "USER"}')

echo "You API key is: $API_KEY"

echo "==== Creating schema ===="
echo ""
curl $API'/schema/import/' -X POST -H 'content-type: application/json' -H "X-Horreum-API-Key: $API_KEY" -d @"$CWD"/assets/schema.json

echo "==== Creating tests ===="
echo ""
if [[ -z "$HORREUM_TEST_ID" ]]; then
    curl $API'/test/import/' -X POST -H 'content-type: application/json' -H "X-Horreum-API-Key: $API_KEY" -d @"$CWD"/assets/test.json
else

    curl $API'/test/import/' -X PUT -H 'content-type: application/json' -H "X-Horreum-API-Key: $API_KEY" -d @"$CWD"/assets/test.json
fi

echo "==== Uploading run ===="
echo ""
curl $API"/run/data?test=${HORREUM_TEST_NAME}&start=$.timestamps.start&stop=$.timestamps.stop&owner=dev-team&access=PUBLIC&schema=${HORREUM_SCHEMA_URI}" -X POST -H 'content-type: application/json' -H "X-Horreum-API-Key: $API_KEY" -d @"$CWD"/assets/baseline_1.json
#curl $API"/run/data?test=${HORREUM_TEST_NAME}&start=$.timestamps.start&stop=$.timestamps.stop&owner=dev-team&access=PUBLIC&schema=${HORREUM_SCHEMA_URI}" -X POST -H 'content-type: application/json' -H "X-Horreum-API-Key: $API_KEY" -d @"$CWD"/assets/run_1.json
