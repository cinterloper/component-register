package net.iowntheinter.vertx.coreLauncher.cluster

import io.vertx.core.AbstractVerticle
import io.vertx.core.AsyncResult
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.core.spi.cluster.ClusterManager
import io.vertx.spi.cluster.impl.zookeeper.ZookeeperClusterManager
import net.iowntheinter.vertx.coreLauncher.CRContext
import net.iowntheinter.vertx.coreLauncher.cluster.impl.embeddedZookeeper
import net.iowntheinter.vertx.coreLauncher.coreStarter
import net.iowntheinter.vertx.util.resourceLoader
import org.apache.zookeeper.server.ZooKeeperServer
import org.apache.zookeeper.server.ZooKeeperServerMain

/**
 * Created by grant on 4/11/16.
 */
class zookeeperVertxStarter implements CRContext{
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
                println("We have a clustered vertx ${vertx.getOrCreateContext()}")
            } else {
                cb([success:false, vertx:null])
                println("there was a failure starting zookeeper & vertx ${vertx.getOrCreateContext()}")
                System.exit(-1)
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
