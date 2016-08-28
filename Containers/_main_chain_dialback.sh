#listen on a channel named by our registration id, for commands from the bus.
cd /opt/lash/lib/chain/
CFG_PATH=/opt/lash/lib/chain/config/

function send_message() {
  vxc -c $CORNERSTONE_HOST:7000 -n $RETURN_ADDR
}
function listen_channels() {
  vxc -c $CORNERSTONE_HOST:7000 -l -n $LAUNCHID
}
source config/config.sh
#^you can overide the send_message or listen_channels methods
#this ends up including everything in /opt/lash/lib/chain/config/include.list
source chain.sh


export METHOD=EB DECODE_SILENT=TRUE

  listen_channels | while read JSON_STRING
  do
   decodeJson && lookup_command && run_task
    if [ "$RETURN_ADDR" != "" ]
    then
      KEY_SET="$OUTPUT_KEYS OUTPUT" encodeJson | send_message
      unset RETURN_ADDR OUTPUT_KEYS OUTPUT
    fi
   reset_chain
   export METHOD=EB
  done


