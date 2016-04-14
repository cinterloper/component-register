package net.iowntheinter.vertx.componentRegister.impl;

import io.vertx.core.AbstractVerticle
import io.vertx.core.json.JsonObject;
import io.vertx.core.Vertx

public class coreLauncher extends AbstractVerticle {

    Vertx vertx;
    def  ct
    def  dm;
    JsonObject config;

    public static void main(String[] args) {
        def c = this.newInstance();
        c.vertx = Vertx.vertx()
        c.start();
    }

    coreLauncher() {
        println("reached CoreLauncher inside vert.x")

    }

    coreLauncher(JsonObject config) {
        this.config = config
        coreLauncher()
    }

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
    }
}
