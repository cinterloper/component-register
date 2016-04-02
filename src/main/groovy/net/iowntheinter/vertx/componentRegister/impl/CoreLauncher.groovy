package net.iowntheinter.vertx.componentRegister.impl;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.Vertx
import net.iowntheinter.vertx.componentRegister.deploymentManager
import net.iowntheinter.vertx.componentRegister.impl.gremlinSystemTracker;
import net.iowntheinter.vertx.componentRegister.componentTracker

public class CoreLauncher extends AbstractVerticle {

    Vertx vertx;
    def  ct
    def  dm;
    JsonObject config;

    public static void main(String[] args) {
        def c = this.newInstance();
        c.vertx = Vertx.vertx()
        c.start();
    }

    CoreLauncher() {
        println("hello")

    }

    CoreLauncher(JsonObject config) {
        this.config = config
        CoreLauncher()
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
