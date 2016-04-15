package net.iowntheinter.vertx.componentRegister.impl

import com.englishtown.promises.When
import com.englishtown.promises.WhenFactory
import com.englishtown.vertx.promises.WhenEventBus
import com.englishtown.vertx.promises.WhenHttpClient
import com.englishtown.vertx.promises.WhenVertx
import com.englishtown.vertx.promises.impl.DefaultWhenEventBus
import com.englishtown.vertx.promises.impl.DefaultWhenHttpClient
import com.englishtown.vertx.promises.impl.DefaultWhenVertx
import com.englishtown.vertx.promises.impl.VertxExecutor
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.eventbus.EventBus
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory

/**
 * Created by grant on 4/14/16.
 */
class whenDeploymentManager {
    When when
    Vertx vertx
    Logger logger
    WhenVertx whenVertx
    WhenEventBus whenEventBus
    WhenHttpClient whenHttpClient
    Map DeploymentList


    void startReady(JsonObject msg){

    }


    whenDeploymentManager(Vertx vertx) {
        this.vertx = vertx

        // Use the vert.x executor to queue callbacks on the vert.x event loop
        when = WhenFactory.createFor({ new VertxExecutor(vertx) })
        logger = LoggerFactory.getLogger(this.class.getName())
        // Instantiate when.java vert.x wrappers
        whenVertx = new DefaultWhenVertx(vertx, when);
        whenEventBus = new DefaultWhenEventBus(vertx, when);
        whenHttpClient = new DefaultWhenHttpClient(vertx, when);
        def eb = whenEventBus.eventBus
        eb.consumer("shutdown", { message ->
            shutdown({}, message.body());
        })

        eb.consumer("startReady", { message ->
            logger.info("ready : " + message.body())
            startReady(launchgrp, message.body())
        })
    }

    void submit(JsonObject deploymentList) {

    }

    void shutdown(ctx, message) {
        logger.error("got message on the shutdown channel: " + (message))
        logger.error(" undeploying")
        logger.info("dep ids:" + v.deploymentIDs() + '\n')
        vertx.deploymentIDs().forEach({ name ->
            vertx.undeploy(name, { res, res_err ->
                if (res_err == null) {
                    logger.info("Undeployed :" + name + '\n');
                } else {
                    logger.error("Undeploy failed for " + name + '\n');
                }
            } as Handler)
        })
        vertx.close({ res ->
            def rt = Runtime.getRuntime()
            rt.halt(-1)
        })
    }

}
