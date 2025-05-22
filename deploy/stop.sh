#!/bin/bash

podman-compose -f compose-devservices.yml down 
# podman volume rm deploy_local_horreum_db
