package net.iowntheinter.vertx.coreLauncher.cluster

import io.vertx.core.AbstractVerticle
import io.vertx.core.AsyncResult
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.core.spi.cluster.ClusterManager
import io.vertx.spi.cluster.impl.zookeeper.ZookeeperClusterManager
import net.iowntheinter.vertx.coreLauncher.cluster.impl.embeddedZookeeper
import net.iowntheinter.vertx.coreLauncher.coreStarter
import net.iowntheinter.vertx.util.resourceLoader
import org.apache.zookeeper.server.ZooKeeperServer
import org.apache.zookeeper.server.ZooKeeperServerMain

/**
 * Created by grant on 4/11/16.
 */
class zookeeperVertxStarter {
    def eZk
    ZooKeeperServerMain zu;
    Vertx vertx
    URLClassLoader classloader = (URLClassLoader) (Thread.currentThread().getContextClassLoader())

    void startZk() {
        try {
            InputStream zkpStream = classloader.
                    getResourceAsStream('example/zookeeperlocal.properties')

            Properties prop = new Properties();
            prop.load(zkpStream)
            eZk = new embeddedZookeeper(prop)



        } catch (e) {
            println("error loading zookeper configuration ${e.printStackTrace()}")
            coreStarter.halt()
        }
    }

    void stopZk() {
        def e = eZk as embeddedZookeeper
        e.threadHandle.interrupt()
    }

    void start(VertxOptions opts) {
        startZk()

        Properties zkConfig = new Properties();
        zkConfig.setProperty("hosts.zookeeper", "127.0.0.1");
        zkConfig.setProperty("path.root", "io.vertx");
        zkConfig.setProperty("retry.initialSleepTime", "1000");
        zkConfig.setProperty("retry.intervalTimes", "3");

        ClusterManager mgr = new ZookeeperClusterManager(zkConfig);
        VertxOptions options = new VertxOptions().setClusterManager(mgr);

        Vertx.clusteredVertx(options, {res ->
            if (res.succeeded()) {
                Vertx vertx = res.result();
                println("We have a clustered vertx ${vertx.getOrCreateContext()}")
            } else {
                // failed!
            }
        });
      //  vertx = Vertx.clusteredVertx(opts, startupResult)
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
