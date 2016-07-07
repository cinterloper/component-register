package net.iowntheinter.componentRegister.impl

import groovy.json.JsonSlurper
import io.vertx.core.AbstractVerticle
import io.vertx.core.AsyncResult
import io.vertx.core.DeploymentOptions
import io.vertx.core.Vertx
import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
import io.vertx.core.shareddata.LocalMap
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.sockjs.BridgeOptions
import io.vertx.ext.web.handler.sockjs.PermittedOptions
import io.vertx.ext.web.handler.sockjs.SockJSHandler
import net.iowntheinter.kvdn.kvserver
import net.iowntheinter.componentRegister.component.impl.DockerTask
import net.iowntheinter.componentRegister.component.impl.VXVerticle
import net.iowntheinter.coreLauncher.impl.waitingLaunchStrategy
import net.iowntheinter.util.displayOutput

public class coreLauncher extends AbstractVerticle {

    def ct
    JsonObject config;
    JsonObject dps = new JsonObject()
    Map launchIds = [:]

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
        Map launchTasks = [:]

        this.config = vertx.getOrCreateContext().config()

        launchTasks = [
                docker: config.getJsonObject('startup').getJsonObject('ext').getJsonObject('docker').getMap().keySet(),
                vertx : config.getJsonObject('startup').getJsonObject('vx').getMap().keySet()
        ]


        logger.debug(vertx)
        logger.debug("reached CoreLauncher inside vert.x, config: ${config}")

        //start all the docker components first in case the cluster manager is one of them

        Closure startContainers = { cb ->
            config.getJsonObject('startup').getJsonObject('ext').getJsonObject('docker').getMap().each { name, cfg ->
                cfg = cfg as JsonObject
                getVertx().sharedData().getLocalMap("cornerstone_components").putIfAbsent("docker:" + name, cfg)
                if (cfg.getBoolean("enabled")) {
                    def Id = UUID.randomUUID().toString()
                    launchIds[Id] = [launchId: Id, launchName: "docker:${name}", startReady: false]  //add docker id
                    def cconfig = (cfg as JsonObject).put('launchId', Id)
                    logger.debug("container config: ${cconfig}")
                    startContainer(name as String, cconfig, {
                        getVertx().sharedData().getLocalMap("cornerstone_deployments").putIfAbsent("docker:" + name, cfg)

                    })
                }
            }
        }


        Closure startVerticles = { cb ->
            config.getJsonObject('startup').getJsonObject('vx').getMap().each { name, vconfig ->
                vconfig = vconfig as JsonObject
                getVertx().sharedData().getLocalMap("cornerstone_components").putIfAbsent(name, vconfig)
                def Id = UUID.randomUUID().toString()

                if (vconfig.getBoolean("enabled")) {
                    launchIds[Id] = [launchId: Id, launchName: "vertx:${name}", startReady: false]  //add docker id
                    startVerticle(name as String, (vconfig as JsonObject).put('launchId', Id), { Map result ->
                        launchIds[Id]['vxid'] = result.name
                        getVertx().sharedData().getLocalMap("cornerstone_deployments").putIfAbsent(name, vconfig)
                    })

                }
            }
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

//main work starts here


        Closure populateMessage = { Map startmessage ->

            LocalMap comp = vertx.sharedData().getLocalMap("cornerstone_components")
            def d = startmessage.data;
            comp.keySet().each { key ->
                d[key] = [" --- ", comp.get(key).getBoolean("enabled").toString()]
            }

            LocalMap depl = vertx.sharedData().getLocalMap("cornerstone_deployments")

            depl.keySet().each { key ->
                println("debug : deployed : ${key}")
                d[key] = ["running", comp.get(key).getBoolean("enabled").toString()]
            }
            startmessage.data = d
            return startmessage
        }

        Map sm = ["header": "coreLauncher",
                  "cols"  : ["COMPONENT", "STATUS", "ENABLED"],
                  "data"  : [:]
        ]

        def s = new kvserver()
        def v = vertx as Vertx
        def router = Router.router(v)
        router.route().handler(BodyHandler.create())
        s.init(router, v, {
            try {
                def server = v.createHttpServer()//configure keystore
                def sjsh = SockJSHandler.create(v)
                def options = new BridgeOptions()
                        .addOutboundPermitted(new PermittedOptions()
                        .setAddressRegex(".*")); //apply security here
                sjsh.bridge(options)
                router.route("/eb/*").handler(sjsh)
                server.requestHandler(router.&accept).listen(config.getInteger('kvdn_port'))
                logger.debug("server port: ${server.actualPort()}")

                display_writer_channel()
                listen_registrations()
                startContainers({})
                startVerticles(vertx)
                def dd = sm.data;
                dd['kvdn'] = [" port: ${server.actualPort()}", "true"]
                sm.data = dd
                getVertx().eventBus().send('_cornerstone:display', new JsonObject(populateMessage(sm) as Map))

            } catch (e) {
                logger.error "could not setup http server:" + e.getMessage()
            }
        })


    }


    void startContainer(String name, JsonObject cfg, Closure cb) {
        logger.debug "ctr: ${name} cfg: ${cfg}"
        logger.debug "\n total config ${config}\n"
        def cfname = new JsonObject(cfg as String).getString('dkrOptsRef')
        logger.debug "cfname ${cfname}"
        Map ctrcfg = (new JsonSlurper().parseText(
                config.getJsonObject('optionBlocks').getJsonObject(cfname).toString())) as Map
        def env_ents = ctrcfg.get("Env") ?: []
        env_ents += ["LAUNCHID=${cfg.getString('launchId')}"]
        ctrcfg.put("Env", env_ents)
        logger.debug "ctrcfg ${ctrcfg}"
        def nd = new DockerTask([name: name, tag: 'latest', image: cfg.getString('image'), ifExists: cfg.getString('ifExists')], ctrcfg)
        def nt = new waitingLaunchStrategy(nd, new JsonObject(cfg as String).getJsonArray('deps').getList())
        nt.start({ result ->
            logger.debug "docker start result: " + result
        })
    }


    void startVerticle(String name, JsonObject vconfig, Closure cb) {
        logger.debug("${name}:${vconfig}")
        def nv = new VXVerticle(vertx, new DeploymentOptions([config: config.put('launchId', vconfig.getString('launchId'))]), name)
        def nt = new waitingLaunchStrategy(nv, new JsonObject(vconfig as String).getJsonArray('deps').getList())
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

    private void listen_registrations() {
        def eb = vertx.eventBus()
        def depchdl = eb.consumer("_cornerstone:registration")
        Map announce = ["header": "coreLauncher",
                        "cols"  : ["COMPONENT", "STATUS", "ENABLED"],
                        "data"  : [:]
        ]
        depchdl.handler({ msg ->
            logger.debug("registration message: " + msg.body())
            launchIds[msg.body()]['startReady'] = true
            def start = true
            def d = announce.data;

            def togo = [:]
            launchIds.each { id, props ->
                d[props.launchName] = [" running ", "true"]
                if (!props['startReady']){
                    start = false
                    togo[id] = props.launchName
                }
            }
            logger.debug("components that still need to check in: ${togo.values()}")
            if (start) {
                getVertx().eventBus().send('_cornerstone:start', 'true')
                logger.debug("sending start sig")
                getVertx().eventBus().send('_cornerstone:display', new JsonObject(announce))

            } else {
                logger.debug("launchids ${launchIds}")
            }

        })
    }

    private void display_writer_channel() {
        def eb = vertx.eventBus()
        def depchdl = eb.consumer("_cornerstone:display")
        depchdl.handler({ Message msg ->
            try {
                Map m = (new JsonSlurper()).parseText(msg.body().toString()) as Map
                new displayOutput(this.config.getString("output_type") ?: "json").display(m)
            } catch (Exception e) {
                logger.error(e)
            }
        })

    }


}
