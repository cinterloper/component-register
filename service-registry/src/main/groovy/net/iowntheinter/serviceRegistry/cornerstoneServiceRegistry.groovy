 | grep dev
 package net.iowntheinter.serviceRegistry

import io.vertx.core.AbstractVerticle
import io.vertx.core.Vertx
import io.vertx.servicediscovery.ServiceDiscovery
import io.vertx.servicediscovery.ServiceDiscoveryOptions

/**
 * Created by g on 7/16/16.
 */
class cornerstoneServiceRegistry  {
    public ServiceDiscovery discovery;
    Vertx vertx
   cornerstoneServiceRegistry(Vertx v, String name){
       vertx = v
       discovery = ServiceDiscovery.create(vertx);

// Customize the configuration
       discovery = ServiceDiscovery.create(vertx,
               new ServiceDiscoveryOptions()
                       .setAnnounceAddress("_cornerstone:service")
                       .setName( "cornerstone_${vertx.getOrCreateContext().config().getString("_root_launch_id") ?: vertx.getOrCreateContext().deploymentID()}:$name"));

    }



    void halt(){
        discovery.close();
    }





}
