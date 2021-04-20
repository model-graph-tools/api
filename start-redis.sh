#!/bin/bash

# Starts a Redis server

docker run \
  --detach \
  --ulimit memlock=-1:-1 \
  --memory-swappiness=0 \
  --name mgt-redis \
  --publish 6379:6379 \
  redis
