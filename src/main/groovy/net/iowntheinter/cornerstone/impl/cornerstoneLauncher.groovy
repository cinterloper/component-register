package net.iowntheinter.cornerstone.impl

import io.vertx.core.AbstractVerticle
import io.vertx.core.Vertx
import io.vertx.core.http.HttpServerOptions
import io.vertx.core.json.JsonArray
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import net.iowntheinter.cornerstone.CornerstoneVerticle
import net.iowntheinter.kvdn.kvserver
import net.iowntheinter.util.displayOutput
import net.iowntheinter.util.http.routeProvider

/**
 * Created by g on 11/13/16.
 */
class cornerstoneLauncher extends CornerstoneVerticle {


    @Override
    void cstart() throws Exception {
        StartKVDN({


        },{ e ->
            logger.fatal("Could not load configured kvdn_route_provider: " + e)
            e.printStackTrace()
            System.exit(-1)
        })

    }
    @Override
    void cstop() throws Exception {
        println "stopping"
    }

    private void StartExternalComponents(cb,ecb){}

    private void StartVerticles(cb,ecb){
        //dowork



        StartExternalComponents(cb,ecb)
    }

    private void StartKVDN(cb,ecb){
        def kvs = new kvserver(vertx)
        def vertx = vertx as Vertx
        def router = Router.router(vertx)
        router.route().handler(BodyHandler.create())
        if (config.containsKey("kvdn_route_providers")) {
            JsonArray KRP = config.getJsonArray("kvdn_route_providers")
            KRP.toList().each { value ->

                try {
                    def instance = this.class.classLoader.loadClass(value as String)?.newInstance() as routeProvider
                    instance.addRoutes(router, vertx)
                } catch (e) {
                    ecb(e)
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
                    server = vertx.createHttpServer(
                            new HttpServerOptions(config.getJsonObject('http_server_options')))
                } else
                    server = vertx.createHttpServer()

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
}
