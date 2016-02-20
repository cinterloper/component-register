import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory

//this is javascript half-converted to groovy...... psdocode till future notice


//requires: check to see if all the dependencies of a component are started

def logger = LoggerFactory.getLogger(this.class.getName())
logger.info("Starting up...");

def eb = vertx.eventBus()
def launchgrp = {}
def ctx = vertx.getOrCreateContext()
def config = ctx.config()
console.log("startup config: " + JSON.stringify(config));


if (config == new JsonObject()) {
    shutdown({}, "no configuration passed with -conf?");
}
else {
    logger.info("startup config: " + JSON.stringify(config, null, 2));
    def svrOpts = [
        "config": config
    ]
    def dbOpts = [
        "config": config,
        "worker": true
    ]
    //everything launches from here
    launchgrp[0] = ["v": "saltConnection.groovy", "opts": svrOpts, "startReady": false]
    launchgrp[1] = ["v": "directoryConnection.groovy", "opts": dbOpts, "startReady": false]
    launchgrp[2] = ["v": "userInterface.groovy", "opts": svrOpts, "startReady": false]
}


eb.consumer("shutdown", { message ->
    shutdown({}, message.body());
})


Closure deploy(ctx, lgrp) {

    //logger.info("lgrp: "+JSON.stringify(lgrp))
    for (def ele in lgrp) {
        logger.info("launching " + JSON.stringify(lgrp[ele].v) + '\n')
        vertx.deployVerticle(lgrp[ele].v, lgrp[ele].opts, { res, err ->
            if (err) {
                logger.error("deploy of " + err.printStackTrace() + " failed \n");
            } else {
                logger.info("deploy of " + res.toString() + " completed \n");
            }
        })
    }

}


Closure startReady(lgrp, message) {
    def start = true;
    for (def ele in lgrp) {
        if (message.name == lgrp[ele].v) {
            lgrp[ele].startReady = true
            // logger.info("setting startReady=true for "+lgrp[ele].v)
        }
        if (lgrp[ele].startReady == false) {
            start = false;
        }
    }
    if (start) {
        eb.publish('start', true)
    }
    else {
        logger.info("still waiting for verticles to check in: ")
    }


}


Closure shutdown(ctx, message) {
    logger.error("got message on the shutdown channel: " + JSON.stringify(message))
    logger.error(" undeploying")
    logger.info("dep ids:" + vertx.deploymentIDs() + '\n')
    vertx.deploymentIDs().forEach({ name ->
        vertx.undeploy(name, { res, res_err ->
            if (res_err == null) {
                logger.info("Undeployed :" + name + '\n');
            } else {
                logger.error("Undeploy failed for " + name + '\n');
            }
        })
    })
    vertx.close({ res ->
        def rt = Java.type("java.lang.Runtime").getRuntime()
        rt.halt(-1)
    })
}

eb.consumer("startReady", { message ->

    logger.info("ready : " + JSON.stringify(message.body()))
    startReady(launchgrp, message.body())
})


deploy({}, launchgrp)