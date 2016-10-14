package net.iowntheinter.coreLauncher.impl

import groovy.json.JsonSlurper
import io.vertx.core.AbstractVerticle
import io.vertx.core.AsyncResult
import io.vertx.core.DeploymentOptions
import io.vertx.core.Vertx
import io.vertx.core.http.HttpServerOptions
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import net.iowntheinter.componentRegister.impl.registrationManager
import net.iowntheinter.coreLauncher.launchStrategy
import net.iowntheinter.kvdn.kvserver
import net.iowntheinter.util.http.routeProvider
import net.iowntheinter.componentRegister.component.impl.DockerTask
import net.iowntheinter.componentRegister.component.impl.VXVerticle
import net.iowntheinter.util.displayOutput
import net.iowntheinter.util.injector

public class coreLauncher extends AbstractVerticle {

    JsonObject config;
    JsonObject dps = new JsonObject()
    Map<String, Map> launchIds = [:]
    def rm;
    def logger = LoggerFactory.getLogger(this.class.getName())

    public void final_shutdown() {
        vertx.close()
    }


    private void vx_shutdown() {
        def int ctr = getVertx().deploymentIDs().size()
        getVertx().deploymentIDs().each { id ->
            getVertx().undeploy(id, { res ->
                ctr--
                if (ctr == 0)
                    final_shutdown()
            })
        }
    }

    @Override
    public void start() throws Exception {

        this.config = vertx.getOrCreateContext().config()

        logger.debug(vertx)
        logger.debug("reached CoreLauncher inside vert.x, config: ${config}")

        //start all the docker components first in case the cluster manager is one of them

        config.getJsonObject('startup').getJsonObject('ext').getJsonObject('docker').getMap().each { name, cfg ->
            cfg = cfg as JsonObject
            if (cfg.getBoolean("enabled")) {
                def Id = UUID.randomUUID().toString()
                launchIds[Id] = [launchId: Id, type: "docker", launchName: "docker:${name}", name: name, config: cfg, startReady: false]
            }
        }
        config.getJsonObject('startup').getJsonObject('vx').getMap().each { name, vconfig ->
            vconfig = vconfig as JsonObject
            def Id = UUID.randomUUID().toString()
            if (vconfig.getBoolean("enabled")) {
                launchIds[Id] = [launchId: Id, type: 'vertx', launchName: "vertx:${name}", name: name, config: vconfig, startReady: false]
                //add docker id
            }
        }
        rm = new registrationManager(launchIds, vertx)
        rm.listen_registrations() // this is quite important


        Closure startContainers = { cb ->
            launchIds.each { String Id, Map launch_task ->
                def docker_crconfig = launch_task.config as JsonObject
                def name = launch_task.name

                if (docker_crconfig.getBoolean("enabled") && (launch_task.type == "docker")) {
                    getVertx().sharedData().getLocalMap("cornerstone_components").putIfAbsent("docker:" + name, docker_crconfig)
                    def cconfig = docker_crconfig.put('launchId', Id)
                    logger.debug("container config: ${cconfig}")

                    Set enviornmentInjections = []
                    logger.info("before _injecting, cconfig.keys ${cconfig.fieldNames()}")
                    if (cconfig.fieldNames().contains("environment_injectors")) {
                        logger.info("_injecting")
                        def ij
                        def IJS = cconfig.getJsonArray("environment_injectors")
                        def IJCount = IJS.size()
                        IJS.each { String ijname ->
                            ij = this.class.classLoader.
                                    loadClass(this.config.getJsonObject('injectors').getString(ijname))?.newInstance() as injector
                            logger.info("did we get an injector?: ${ij}")
                            ij.inject(docker_crconfig, vertx, { result ->
                                if (result.error == null) {
                                    enviornmentInjections += result.result
                                    IJCount--
                                } else {
                                    ((Exception) result.error).printStackTrace()
                                    logger.fatal("Could not load configured environment injector : " + result.error)
                                    System.exit(-1)
                                }



                                def env = cconfig.getJsonArray("Env") ?: new JsonArray()
                                enviornmentInjections.each { String needle ->
                                    env.add(needle)
                                }
                                cconfig.put("Env", env)
                                if (IJCount == 0) { //last async cb fired
                                    startContainer(name as String, cconfig, {
                                        getVertx().sharedData().getLocalMap("cornerstone_deployments").putIfAbsent("docker:" + name, docker_crconfig)
                                    })
                                }

                            })


                        }

                    }


                }
            }
        }


        Closure startVerticles = { cb ->
            launchIds.each { String Id, Map launch_task ->
                def vconfig = launch_task.config as JsonObject
                def name = launch_task.name

                if (vconfig.getBoolean("enabled") && launch_task.type == "vertx") {
                    getVertx().sharedData().getLocalMap("cornerstone_components").putIfAbsent(name, vconfig)
                    startVerticle(name as String, (vconfig as JsonObject).put('launchId', Id), { Map result ->
                        launchIds[Id]['vxid'] = result.name
                        getVertx().sharedData().getLocalMap("cornerstone_deployments").putIfAbsent(name, vconfig)
                    })

                }
            }
        }




        Map startMessage = ["header": "coreLauncher",
                            "cols"  : ["COMPONENT", "STATUS", "ENABLED"],
                            "data"  : [:]
        ]

        def kvs = new kvserver(vertx)
        def v = vertx as Vertx
        def router = Router.router(v)
        router.route().handler(BodyHandler.create())
        if (config.containsKey("kvdn_route_providers")) {
            JsonArray KRP = config.getJsonArray("kvdn_route_providers")
            KRP.toList().each { value ->

                try {
                    def instance = this.class.classLoader.loadClass(value as String)?.newInstance() as routeProvider
                    instance.addRoutes(router, v)
                } catch (e) {
                    logger.fatal("Could not load configured kvdn_route_provider: " + e)
                    e.printStackTrace()
                    System.exit(-1)
                }

            }


        }
        /**
         * initalize the key value server, and activate any configured @Link:routeProvider s
         */

        kvs.init(router, {
            try {
                def server
                if (config.containsKey('http_server_options')) {
                    server = v.createHttpServer(
                            new HttpServerOptions(config.getJsonObject('http_server_options')))
                } else
                    server = v.createHttpServer()

                server.requestHandler(router.&accept).listen(config.getInteger('kvdn_port'))
                logger.debug("server port: ${config.getInteger('kvdn_port')}")

                startVerticles(vertx)
                startContainers({})

                def dispd = startMessage.data
                dispd['kvdn'] = [" port: ${config.getInteger('kvdn_port')}", "true"]
                startMessage.data = dispd
                new displayOutput().display(startMessage)
            } catch (e) {
                logger.error "error during deploy:" + e.getMessage()
                e.printStackTrace()
            }
        })


    }


    void startContainer(String name, JsonObject cconfig, Closure cb) {
        logger.debug "ctr: ${name} cfg: ${cconfig}"
        logger.debug "\n total config ${config}\n"
        def cfname = new JsonObject(cconfig as String).getString('dkrOptsRef')
        logger.debug "cfname ${cfname}"
        Map ctrcfg = (new JsonSlurper().parseText(
                config.getJsonObject('optionBlocks').getJsonObject(cfname).toString())) as Map
        def env_ents = ctrcfg.get("Env") ?: []
        env_ents += ["LAUNCHID=${cconfig.getString('launchId')}"]
        env_ents += cconfig.getJsonArray('Env')
        ctrcfg.put("Env", env_ents)
        logger.debug "ctrcfg ${ctrcfg}"
        def nd = new DockerTask([name: name, tag: 'latest', image: cconfig.getString('image'), ifExists: cconfig.getString('ifExists')], ctrcfg)

        def strategy = this.config.getString("default_launch_strategy")
        def nt
        if (cconfig.containsKey('launchStrategy'))
            strategy = cconfig.getString('launchStrategy')
        if (strategy == 'waiting')
            nt = new waitingLaunchStrategy(vertx, cconfig.getString('launchId'), nd, new JsonObject(cconfig as String).getJsonArray('deps').getList())
        else
            nt = new coordinatedLaunchStrategy(vertx, cconfig.getString('launchId'), nd, new JsonObject(cconfig as String).getJsonArray('deps').getList())
        try {
            assert nt != null
            nt.start({ result ->
                logger.debug "docker start result: " + result
            })
        } catch (e) {
            logger.error(e)
        }

    }


    void startVerticle(String name, JsonObject vconfig, Closure cb) {
        logger.debug("${name}:${vconfig}")
        def nv = new VXVerticle(vertx, new DeploymentOptions([config: config.put('launchId', vconfig.getString('launchId'))]), name)
        def nt = new coordinatedLaunchStrategy(vertx, vconfig.getString('launchId'), nv, new JsonObject(vconfig as String).getJsonArray('deps').getList())
        nt.start({ AsyncResult result ->
            String id
            if (result.succeeded()) {
                id = result.result()
                logger.debug("Started verticle ${id}")
                if (vertx.sharedData().getLocalMap('deployments').get(name)) {
                    dps = vertx.sharedData().getLocalMap('deployments').get(name) as JsonObject
                }
                dps.put(id, [name: "verticle"])
                vertx.sharedData().getLocalMap('deployments').put(name, dps)
                logger.debug(vertx.sharedData().getLocalMap('deployments').get(name))
                vertx.eventBus().send('task_deployments', new JsonObject([name: id]))
                cb([name: id, error: null])
            } else {
                logger.error "deployment failed: ${name} :\n ${result.cause()}"
                cb([name: null, error: result.cause()])
            }
        })
    }


}
