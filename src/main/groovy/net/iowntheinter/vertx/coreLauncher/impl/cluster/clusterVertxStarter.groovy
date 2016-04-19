package net.iowntheinter.vertx.coreLauncher.impl.cluster

import io.vertx.core.AsyncResult
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.core.logging.LoggerFactory

import net.iowntheinter.util.cluster.EZKCMFactory
import net.iowntheinter.vertx.coreLauncher.coreCTX
import net.iowntheinter.util.embedded.embeddedZookeeper
import net.iowntheinter.vertx.coreLauncher.impl.coreStarter
import org.apache.zookeeper.server.ZooKeeperServerMain

/**
 * Created by grant on 4/11/16.
 */
class clusterVertxStarter implements coreCTX {

    Vertx vertx
    def logger = LoggerFactory.getLogger(this.class.getName())

    URLClassLoader classloader = (URLClassLoader) (Thread.currentThread().getContextClassLoader())

    void start(VertxOptions opts, Closure<Map> cb) {
//make this pluggable by configuration and strip zookeeper impl into external jar
        VertxOptions options = new VertxOptions().setClusterManager(new EZKCMFactory().getMgr());

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
            logger.u("Started clustered vertx:")
        }


    }
}
