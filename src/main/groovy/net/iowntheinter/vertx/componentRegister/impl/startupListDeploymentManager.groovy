package net.iowntheinter.vertx.componentRegister.impl

import com.englishtown.promises.Promise
import io.vertx.core.AsyncResult
import io.vertx.core.DeploymentOptions
import io.vertx.core.DeploymentOptionsConverter
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import net.iowntheinter.vertx.componentRegister.deploymentManager


//requires: check to see if all the dependencies of a component are started

class startupListDeploymentManager implements deploymentManager {
    Vertx v;
    Logger logger

    startupListDeploymentManager(Vertx vx) {

    }

    startupListDeploymentManager(Vertx vx, JsonObject launchCfg) {

    }

    void processConfig(ctx, cb) {

    }


    void deploy(name, cfg, cb) {


    }


    void startReady(lgrp, message) {
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
        } else {
            logger.info("still waiting for verticles to check in: ")
        }


    }


    void shutdown(ctx, message) {
        logger.error("got message on the shutdown channel: " + (message))
        logger.error(" undeploying")
        logger.info("dep ids:" + v.deploymentIDs() + '\n')
        v.deploymentIDs().forEach({ name ->
            v.undeploy(name, { res, res_err ->
                if (res_err == null) {
                    logger.info("Undeployed :" + name + '\n');
                } else {
                    logger.error("Undeploy failed for " + name + '\n');
                }
            } as Handler)
        })
        v.close({ res ->
            def rt = Runtime.getRuntime()
            rt.halt(-1)
        })
    }


    void realInit() {


        def logger = LoggerFactory.getLogger(this.class.getName())
        logger.info("Starting up...");

        def eb = v.eventBus()
        def launchgrp = [:]
        def config = v.getOrCreateContext().config()
        console.log("startup config: " + (config));






        if (config == new JsonObject()) {
            shutdown([:], "no configuration passed with -conf?");
        } else {
            logger.info("startup config: " + config, null, 2);
            def svrOpts = [
                    "config": config
            ]
            def dbOpts = [
                    "config": config,
                    "worker": true
            ]
            //everything launches from here
            launchgrp[0] = ["v": "example1.groovy", "opts": svrOpts, "startReady": false]
            launchgrp[1] = ["v": "example2.groovy", "opts": dbOpts, "startReady": false]
        }


        eb.consumer("shutdown", { message ->
            shutdown({}, message.body());
        })



        eb.consumer("startReady", { message ->

            logger.info("ready : " + message.body())
            startReady(launchgrp, message.body())
        })


        deploy("groupA", launchgrp, {})
    }

    @Override
    void deploy(String name, JsonObject config, Closure cb) {
        def dc = new DeploymentOptionsConverter()
        def lgrp = config.getJsonArray('')

        def opts = new DeploymentOptions()
        //logger.info("lgrp: "+(lgrp))
        int size = lgrp.size()
        int fails = 0;
        lgrp.each { ele ->
            logger.info("launching " + lgrp[ele] + '\n')
            dc.fromJson(lgrp[ele].opts as JsonObject, opts)
            v.deployVerticle(lgrp[ele].v as String, opts, { res, err ->
                if (err) {
                    size--
                    cb([sucess:false]);
                    logger.error("deploy of " + err.printStackTrace() + " failed \n")
                } else {
                    size--
                    logger.info("deploy of " + res.toString() + " completed \n");
                    if (size == 0)
                        cb([sucess:true])
                }
            } as Handler)
        }

    }

    @Override
    void undeploy(String id, Closure cb) {

    }
}










