#!/usr/bin/bash


# Using local env: HORREUM_BOOTSTRAP_PWD=<pwd> HORREUM_TEST_ID=10 API=http://localhost:18088/api ./scripts/setup_horreum.sh
CWD="$(dirname "$0")"

API=${API:-"http://localhost:8081/"}

echo "==== Loading config ===="
echo ""
curl "$API/config" -X POST -H 'content-type: application/json' -H "X-Horreum-API-Key: $API_KEY" -d @"$CWD"/assets/repo-config.json
