#!/bin/bash

# Builds the api service

./gradlew build -Dquarkus.package.type=native -Dquarkus.native.container-build=true
docker build \
  --file src/main/docker/Dockerfile.native-distroless \
  --tag modelgraphtools/api \
  .
