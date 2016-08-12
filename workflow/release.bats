#!/usr/bin/env bats
source workflow/mulibuild.sh 
@test "release to  github" {
  release
  result=$?
  [ "$result" -eq 0 ]
}
@test "release to docker hub" {
  docker push cinterloper/cornerstone-base
  result=$?
  [ "$result" -eq 0 ]
}

