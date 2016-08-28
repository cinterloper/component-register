#listen on a channel named by our registration id, for commands from the bus.
cd /opt/lash/lib/chain/
CFG_PATH=/opt/lash/lib/chain/config/
source config/config.sh
source chain.sh
export METHOD=EB DECODE_SILENT=TRUE

 vxc -c $CORNERSTONE_HOST:7000 -l -n $LAUNCHID | while read JSON_STRING
  do
   decodeJson && lookup_command && run_task
    if [ "$RETURN_ADDR" != "" ]
    then
      KEY_SET="$OUTPUT_KEYS OUTPUT" encodeJson | vxc -c $CORNERSTONE_HOST:7000 -n $RETURN_ADDR
      unset RETURN_ADDR OUTPUT_KEYS OUTPUT
    fi
   reset_chain
   export METHOD=EB
  done


