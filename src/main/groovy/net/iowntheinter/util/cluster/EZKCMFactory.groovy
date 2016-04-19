package net.iowntheinter.util.cluster

import io.vertx.core.logging.LoggerFactory
import io.vertx.core.spi.cluster.ClusterManager
import net.iowntheinter.util.embedded.embeddedZookeeper
import net.iowntheinter.vertx.coreLauncher.impl.coreStarter
import org.apache.zookeeper.server.ZooKeeperServerMain
import io.vertx.core.spi.cluster.ClusterManager
import io.vertx.spi.cluster.impl.zookeeper.ZookeeperClusterManager
/**
 * Created by grant on 4/19/16.
 */
class EZKCMFactory {
    def eZk
    ZooKeeperServerMain zu;
    ClusterManager mgr
    def logger = LoggerFactory.getLogger(this.class.getName())
    URLClassLoader classloader = (URLClassLoader) (Thread.currentThread().getContextClassLoader())

    EZKCMFactory() {
        startZk()

        Properties zkCmConfig = new Properties();
        zkCmConfig.setProperty("hosts.zookeeper", "127.0.0.1");
        zkCmConfig.setProperty("path.root", "io.vertx");
        zkCmConfig.setProperty("retry.initialSleepTime", "1000");
        zkCmConfig.setProperty("retry.intervalTimes", "3");

        mgr = new ZookeeperClusterManager(zkCmConfig) as ClusterManager;
    }

    private void startZk() {
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

    private void stopZk() {
        def e = eZk as embeddedZookeeper
        e.threadHandle.interrupt()
    }
    public ClusterManager getMgr(){
        return mgr
    }

}
