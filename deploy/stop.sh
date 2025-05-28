#!/bin/bash

podman rm -f local.perf-bot && podman-compose -f compose-devservices.yml down
# podman volume rm deploy_local_horreum_db
