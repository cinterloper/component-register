#!/usr/bin/env bats
source workflow/mulibuild.sh 
@test "build cornerstone base" {
  build
  result=$?
  [ "$result" -eq 0 ]
}

@test "build cornerstone base container" {
  VERSION=$(DUMP_VERSION=true ./gradlew 2>/dev/null)
  export BUILDARGS="--build-arg=PROJVER=$VERSION"
  bash  Containers/build.sh
  result=$?
  [ "$result" -eq 0 ]
}

