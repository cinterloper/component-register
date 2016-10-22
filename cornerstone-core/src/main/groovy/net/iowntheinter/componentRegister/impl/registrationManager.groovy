package net.iowntheinter.componentRegister.impl

import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import net.iowntheinter.kvdn.util.distributedWaitGroup
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
        Map announce = ["header": "SYSTEM HAS LAUNCHED",
                        "data"  : launchIds
        ]
        def wg = new distributedWaitGroup(launchIds.keySet(),{
            this.vertx.eventBus().send('_cornerstone:start', 'true')
            logger.debug("sending start sig")
            new displayOutput().display(announce)
        },vertx)

        wg.onChannel(regchannel)

    }

}
