#!/bin/sh

redis-server --daemonize yes && sleep 1
redis-cli < /data/version-mapping.redis
redis-cli save
redis-cli shutdown
redis-server
