#!/usr/bin/env bats
source workflow/mulibuild.sh 
@test "build cornerstone base" {
  build
  result=$?
  [ "$result" -eq 0 ]
}
@test "build cornerstone base container" {
  bash  Containers/build.sh
  result=$?
  [ "$result" -eq 0 ]
}

