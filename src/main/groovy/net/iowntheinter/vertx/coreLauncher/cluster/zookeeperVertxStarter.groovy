package net.iowntheinter.vertx.coreLauncher.cluster

import io.vertx.core.AbstractVerticle
import io.vertx.core.AsyncResult
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import net.iowntheinter.vertx.coreLauncher.cluster.impl.embeddedZookeeper
import net.iowntheinter.vertx.coreLauncher.coreStarter
import net.iowntheinter.vertx.util.resourceLoader

/**
 * Created by grant on 4/11/16.
 */
@Singleton class zookeeperVertxStarter {
    def eZk
    Vertx vertx
    void startZk() {
        Properties prop = new Properties();
        try {
            eZk = new embeddedZookeeper(
                    prop.load(getClass().
                            getClassLoader().
                            getResourceAsStream('zookeeperlocal.properties')))
        } catch (e) {
            println("error loading zookeper configuration ${e}")
            coreStarter.halt()
        }
    }
    void stopZk() {
        def e = eZk as embeddedZookeeper
        e.threadHandle.interrupt()
    }

    void start(VertxOptions opts) {
        vertx = Vertx.clusteredVertx(opts, startupResult)
    }
    def startupResult = { AsyncResult res ->
        if (res.failed()) {
            println("could not start: ${res.cause()}")
            coreStarter.halt()
        } else {
            println("Started clustered vertx:")
        }


    }
}
