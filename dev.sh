#!/bin/bash

# Starts the api service in dev mode

./gradlew \
  -Dquarkus.http.port=9911 \
  -Dquarkus.log.category.\"org.wildfly.modelgraph\".level=DEBUG \
  quarkusDev
