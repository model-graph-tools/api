#!/bin/bash

# Builds redis

docker build \
  --file src/main/docker/Dockerfile.redis \
  --tag modelgraphtools/redis \
  src/main/docker
