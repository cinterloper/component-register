#!/usr/bin/env bash
fail() {
    echo "ERROR IN BUILD"
    kill -9 $$
}

{
  docker build -t cinterloper/cornerstone-base .
} || {
  fail
}
for ctrdir in $(find . -maxdepth 1 -mindepth 1 -type d | grep -v _)
do
  cd $(realpath $ctrdir);
  {
    docker build -t shadowsystem/${PWD##*/} .
  } || {
    fail
  }
  cd ..;
done



