#!/usr/bin/env bash
source /etc/bash.bashrc
let START_DELAY="$MY_START_TIME"
let START_TRIES=5
export RUN_PID="$$"
export KVDN_BASE_URL=http://$CORNERSTONE_HOST:6500
function on_failure(){
  $FAILURE_HOOK $@
#  kill -9 $RUN_PID
}
function regfunc() {
  kvdn-cli --set --key $LAUNCHID cornerstone/registration
}

source $STARTUP_HOOKS
#^can override regfunc in here

source $LASH_PATH/bin/init.sh


function test_start(){
    let START_COUNT="0"

    while [[ $START_COUNT -lt $START_TRIES && "$STARTED" != "TRUE" ]]
    do
      healthcheck_func
      HEALTH_STAT="$?"
      echo "DEBUG: after health check, if you dont see this hc blocks forever"
      if [ "$HEALTH_STAT" -eq "0" ]
      then  #insert our LAUNCHID into the cornerstone/registration map, with a copy of our capabilities
        echo _C_REG: $(echo $CAPABILITIES | regfunc )
        export STARTED="TRUE"
      else
        export START_COUNT=$((START_COUNT+1))
        echo "set sc $START_COUNT"
      fi
      echo FAILURES: $START_COUNT SLEEPING: $START_DELAY
      sleep $START_DELAY
    done

    if [ "$STARTED" != "TRUE" ]
    then
        on_failure "start tries exceeded, verify health check passes"
    fi
}

echo START: $STARTUP_HOOK
sleep $KVDN_START_TIME
#start the main task and the health check
#if the health check passes before START_TRIES is reached, we will check into the registration system
{
  startup_func
  } || {
  on_failure "startup_func returned non 0"
}&

test_start &

#start the blocking watchdog/monitor process
source $MAIN_LOOP
