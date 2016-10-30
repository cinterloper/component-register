package net.iowntheinter.cornerstone.cluster

import io.vertx.core.AsyncResult
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.core.logging.LoggerFactory

/**
 * Created by grant on 4/11/16.
 */
class clusterVertxStarter {

    Vertx vertx
    def logger = LoggerFactory.getLogger(this.class.getName())

    void start(VertxOptions opts, Closure<Map> cb) {

        Vertx.clusteredVertx(opts, { res ->
            if (res.succeeded()) {
                vertx = res.result();
                cb([result: vertx,error:null])
                logger.info("We have a clustered vertx ${vertx.getOrCreateContext()}")
            } else {
                logger.error("there was a failure starting clustered vertx ")
                res.cause().printStackTrace()
                System.exit(-1)
                // failed!
            }
        })
    }
}
