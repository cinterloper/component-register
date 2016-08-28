#!/usr/bin/env bash
VARS=(BUILDARGS)
for var in ${VARS[@]}
 do
  if [  "$(eval echo \$"$var")" == "" ]
  then
    echo failure: YOU MUST PASS $var
    kill -9 $$
  fi
done

fail() {
    echo "ERROR IN BUILD"
    kill -9 $$
}

log() {
  echo $@
}


{
  docker build $BUILDARGS -t cinterloper/cornerstone-base .
} || {
  fail
}



for ctrdir in $(find Containers/ -maxdepth 1 -mindepth 1 -type d | grep -v _)
do
  cd $(realpath $ctrdir);
  log "building $VENDOR/${PWD##*/} "; echo
  if [[ ! -f _BUILD_DISABLE ]]; then
    if [[ -f add.sh ]]; then
      {
        bash add.sh add $EXTRA
       } || {
        fail
      }
    fi
    {
      docker build -t $VENDOR/${PWD##*/} .
    } || {
      fail
    }
  fi
  cd ../../;
done
