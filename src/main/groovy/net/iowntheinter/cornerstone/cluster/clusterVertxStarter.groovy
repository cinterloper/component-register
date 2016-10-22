package net.iowntheinter.cornerstone.cluster

import io.vertx.core.AsyncResult
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.core.logging.LoggerFactory
import net.iowntheinter.coreLauncher.impl.coreStarter

/**
 * Created by grant on 4/11/16.
 */
class clusterVertxStarter {

    Vertx vertx
    def logger = LoggerFactory.getLogger(this.class.getName())

    void start(VertxOptions opts, Closure<Map> cb) {
        VertxOptions options = new VertxOptions()

        Vertx.clusteredVertx(options, { res ->
            if (res.succeeded()) {
                Vertx vertx = res.result();
                cb([success: true, vertx: vertx])
                logger.info("We have a clustered vertx ${vertx.getOrCreateContext()}")
            } else {
                cb([success: false, vertx: null])
                logger.error("there was a failure starting zookeeper & vertx ${vertx.getOrCreateContext()}")
                System.exit(-1)
                // failed!
            }
        });
        //  vertx = Vertx.clusteredVertx(opts, startupResult)
    }
    def startupResult = { AsyncResult res ->
        if (res.failed()) {
            logger.error("could not start: ${res.cause()}")
            coreStarter.halt()
        } else {
            logger.info("Started clustered vertx:")
        }


    }
}
