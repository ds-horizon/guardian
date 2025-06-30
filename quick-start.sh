#!/usr/bin/env bash
set -eo pipefail

mvn clean package
docker compose down
docker compose up --build --detach
