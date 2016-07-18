package net.iowntheinter.componentRegister.impl

import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import net.iowntheinter.util.displayOutput

/**
 * Created by g on 7/16/16.
 */
class registrationManager {
    Vertx vertx
    Logger logger
    Map launchIds
    registrationManager(Map lid, Vertx v){
        this.vertx=v as Vertx
        this.launchIds = lid
        logger = LoggerFactory.getLogger(this.class.getName())
        logger.debug('Launch ids for new reg manager: '+ launchIds)

    }
    //list locally that we have started it
    private void list_component_started(String depid, String cmpname, cb){

    }


    //register globally that the component has become available
    private void listen_registrations(String regchannel = "_cornerstone:registration") {
        def eb = vertx.eventBus()
        def depchdl = eb.consumer(regchannel)
        Map announce = ["header": "coreLauncher",
                        "cols"  : ["COMPONENT", "STATUS", "ENABLED"],
                        "data"  : [:]
        ]
        println "lanunchids ${launchIds.keySet()}"
        depchdl.handler({ msg ->
            println "got msg : ${msg.body()}"
            logger.debug("registration message: " + msg.body())
            launchIds[msg.body()]['startReady'] = true
            def start = true
            def d = announce.data;

            def togo = [:]
            launchIds.each {  id , props  ->
                id = id as String
                props = props as Map
                d[props.launchName] = [" running ", "true"]
                if (!props['startReady']) {
                    start = false
                    togo[id] = props.launchName
                }
            }
            logger.debug("components that still need to check in: ${togo.values()}")
            if (start) {
                getVertx().eventBus().send('_cornerstone:start', 'true')
                logger.debug("sending start sig")

                new displayOutput().display(announce)

            } else {
                logger.debug("launchids ${launchIds}")
            }

        })
    }

}
