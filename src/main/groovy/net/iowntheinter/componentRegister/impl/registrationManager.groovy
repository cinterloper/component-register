package net.iowntheinter.componentRegister.impl

import io.vertx.core.Vertx
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import net.iowntheinter.kvdn.util.distributedWaitGroup
import net.iowntheinter.cornerstone.util.displayOutput

/**
 * Created by g on 7/16/16.
 */
class registrationManager {
    Vertx vertx
    Logger logger
    final String nodeid
    final Map launchIds
    registrationManager(Map launchIds, Vertx vertx){
        this.nodeid = UUID.randomUUID().toString()
        this.vertx=vertx as Vertx
        this.launchIds = launchIds
        this.vertx.sharedData().getLocalMap("_cornerstone:config").put("nodeid",nodeid)
        logger = LoggerFactory.getLogger(this.class.getName())
        logger.debug('Launch ids for new reg manager: '+ launchIds)

    }

    //register globally that the component has become available
    private void listen_registrations(String regchannel = "_cornerstone:registration") {
        Map announce = ["header": "SYSTEM HAS LAUNCHED",
                        "data"  : launchIds
        ]
        def wg = new distributedWaitGroup(launchIds.keySet(),{
            getVertx().eventBus().send("_cornerstone:start:$nodeid", 'true')
            logger.debug("sending start sig")
            new displayOutput().display(announce)
        },vertx)

        wg.onChannel(regchannel)


    }

}
