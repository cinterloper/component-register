package net.iowntheinter.vertx.coreLauncher.impl.cluster

import io.vertx.core.AsyncResult
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.core.logging.LoggerFactory
import io.vertx.core.spi.cluster.ClusterManager
import io.vertx.spi.cluster.impl.zookeeper.ZookeeperClusterManager
import net.iowntheinter.vertx.coreLauncher.coreCTX
import net.iowntheinter.util.embedded.embeddedZookeeper
import net.iowntheinter.vertx.coreLauncher.impl.coreStarter
import org.apache.zookeeper.server.ZooKeeperServerMain

/**
 * Created by grant on 4/11/16.
 */
class zookeeperVertxStarter implements coreCTX{
    def eZk
    ZooKeeperServerMain zu;
    Vertx vertx
    def logger = LoggerFactory.getLogger(this.class.getName())

    URLClassLoader classloader = (URLClassLoader) (Thread.currentThread().getContextClassLoader())

    void startZk() {
        try {
            InputStream zkpStream = classloader.
                    getResourceAsStream('example/zookeeperlocal.properties')

            Properties prop = new Properties();
            prop.load(zkpStream)
            eZk = new embeddedZookeeper(prop)



        } catch (e) {
            logger.error("error loading zookeper configuration ${e.printStackTrace()}")
            coreStarter.halt()
        }
    }

    void stopZk() {
        def e = eZk as embeddedZookeeper
        e.threadHandle.interrupt()
    }


    void start(VertxOptions opts, Closure<Map> cb) {
        startZk()

        Properties zkCmConfig = new Properties();
        zkCmConfig.setProperty("hosts.zookeeper", "127.0.0.1");
        zkCmConfig.setProperty("path.root", "io.vertx");
        zkCmConfig.setProperty("retry.initialSleepTime", "1000");
        zkCmConfig.setProperty("retry.intervalTimes", "3");

        ClusterManager mgr = new ZookeeperClusterManager(zkCmConfig);
        VertxOptions options = new VertxOptions().setClusterManager(mgr);

        Vertx.clusteredVertx(options, {res ->
            if (res.succeeded()) {
                Vertx vertx = res.result();
                cb([success:true, vertx:vertx])
                logger.info("We have a clustered vertx ${vertx.getOrCreateContext()}")
            } else {
                cb([success:false, vertx:null])
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
