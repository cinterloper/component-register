package net.iowntheinter.vertx.componentRegister.impl

import groovy.json.JsonSlurper
import io.vertx.core.AbstractVerticle
import io.vertx.core.DeploymentOptions
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
import net.iowntheinter.vertx.componentRegister.component.impl.DockerTask
import net.iowntheinter.vertx.componentRegister.component.impl.VXVerticle
import net.iowntheinter.vertx.coreLauncher.impl.waitingLaunchStrategy

public class coreLauncher extends AbstractVerticle  {

    def ct
    JsonObject config;
    JsonObject dps = new JsonObject()
    Map launchTasks
    def logger = LoggerFactory.getLogger(this.class.getName())

    public void final_shutdown(String topic, String value) {
        vertx.close()
    }

    @Override
    public void start() throws Exception {
        launchTasks = [:]
        logger.debug(vertx)
        this.config = vertx.getOrCreateContext().config()
        logger.debug("reached CoreLauncher inside vert.x, cofig: ${config}")

        //start all the docker components first in case the cluster manager is one of them
        config.getJsonObject('startup').getJsonObject('ext').getJsonObject('docker').getMap().each { name,  cfg ->
            cfg = cfg as JsonObject
            getVertx().sharedData().getLocalMap("cornerstone_components").putIfAbsent("docker:"+name, cfg)
            if(cfg.getBoolean("startReady")){
                startContainer(name as String, cfg as JsonObject, {
                    getVertx().sharedData().getLocalMap("cornerstone_deployments").putIfAbsent("docker:"+name, cfg)

                })
            }
        }

        Closure startVerticles = { vertx ->
            config.getJsonObject('startup').getJsonObject('vx').getMap().each { name, vconfig ->
                vconfig = vconfig as JsonObject
                vconfig = vconfig as JsonObject
                getVertx().sharedData().getLocalMap("cornerstone_components").putIfAbsent(name, vconfig)
                if(vconfig.getBoolean("startReady")){
                    startVerticle(name as String, vconfig as JsonObject, {
                        getVertx().sharedData().getLocalMap("cornerstone_deployments").putIfAbsent(""+name, vconfig)

                    })

                }}
        }

        Closure afterVXClusterStart = { Map res ->
            Vertx vx
            logger.debug(res)
            if (!res.success) {
                logger.error("could not start clustered vertx")
                System.exit(-1)
            } else {
                vx = res.vertx as Vertx
                def opts = new DeploymentOptions([config: config.getMap()])
                startVerticles(vx)
            }
        }


        startVerticles(vertx)


    }


    void startContainer(String name, JsonObject cfg, Closure cb) {
        logger.debug "ctr: ${name} cfg: ${cfg}"
        cfg = cfg as JsonObject
        logger.debug "\n total config ${config}\n"
        def cfname = new JsonObject(cfg as String).getString('dkrOptsRef')
        logger.debug "cfname ${cfname}"
        Map ctrcfg = (new JsonSlurper().parseText(
                config.getJsonObject('optionBlocks').getJsonObject(cfname).toString())) as Map

        logger.debug "ctrcfg ${ctrcfg}"
        def nd = new DockerTask([name: name, tag: 'latest', image: cfg.getString('image'), ifExists: cfg.getString('ifExists')], ctrcfg)
        def nt = new waitingLaunchStrategy(nd, new JsonObject(cfg as String).getJsonArray('deps').getList())
        nt.start({ result ->
            logger.info "docker start result: " +
                    (result as Map).container.content.Id
        })
    }


    void startVerticle(String name, JsonObject vconfig, Closure cb) {
        logger.debug("${name}:${vconfig}")
        def nv = new VXVerticle(vertx, new DeploymentOptions([config: config]), name)
        def nt = new waitingLaunchStrategy(nv, new JsonObject(vconfig as String).getJsonArray('deps').getList())
        nt.start({ result ->
            String id
            if (result.succeeded()) {
                id = (result as Future).result()
                logger.info("Started ${id}")
                if (vertx.sharedData().getLocalMap('deployments').get(name)) {
                    dps = vertx.sharedData().getLocalMap('deployments').get(name) as JsonObject
                }
                dps.put(id, [name: "verticle"])
                vertx.sharedData().getLocalMap('deployments').put(name, dps)
                logger.debug(vertx.sharedData().getLocalMap('deployments').get(name))
                vertx.eventBus().send('task_deployments', new JsonObject([name: id]))
            } else {
                logger.error "deployment failed: ${name} :\n ${result.cause()}"

            }

            cb([name: id])
        })
    }


}
