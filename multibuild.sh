#!/usr/bin/env bash
VERSION=$(DUMP_VERSION=true ./gradlew 2>/dev/null)
export BUILDARGS="--build-arg=PROJVER=$(echo $VERSION | cut -d '-' -f 2)"
for ver in 3.3.2 3.2.1; do VERTX_VERSION=$ver ./gradlew clean shadowJar publish; done
