package net.iowntheinter.vertx.componentRegister.impl;

import io.vertx.core.AbstractVerticle
import io.vertx.core.json.JsonObject;
import io.vertx.core.Vertx

public class coreLauncher extends AbstractVerticle {

    def  ct
    def  dm;
    JsonObject config;



    public void final_shutdown(String topic, String value) {
        vertx.close()
    }

    public void start_manager() {
        ct = new gremlinSystemTracker();
        dm = new startupListDeploymentManager(vertx, config)
    }


    @Override
    public void start() throws Exception {
        println(vertx)
        start_manager()
        this.config = vertx.getOrCreateContext().config()
        println("reached CoreLauncher inside vert.x, cofig: ${config}")
        config.getJsonObject('startup').getJsonObject('vx').getMap().each { k , v ->
            println ("${k}:${v}")
        }

    }
}
