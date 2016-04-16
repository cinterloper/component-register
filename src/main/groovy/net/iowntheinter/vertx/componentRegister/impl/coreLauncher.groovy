package net.iowntheinter.vertx.componentRegister.impl;

import io.vertx.core.AbstractVerticle
import io.vertx.core.DeploymentOptions
import io.vertx.core.json.JsonObject;
import io.vertx.core.Vertx
import net.iowntheinter.vertx.componentRegister.componentType.dependentTask
import net.iowntheinter.vertx.componentRegister.componentType.impl.VXVerticle

public class coreLauncher extends AbstractVerticle {

    def ct
    def dm;
    JsonObject config;
    Map launchTasks


    public void final_shutdown(String topic, String value) {
        vertx.close()
    }

    public void start_manager() {
        ct = new gremlinSystemTracker();
        dm = new startupListDeploymentManager(vertx, config)
    }


    @Override
    public void start() throws Exception {
        launchTasks = [:]
        println(vertx)
        start_manager()
        this.config = vertx.getOrCreateContext().config()
        println("reached CoreLauncher inside vert.x, cofig: ${config}")
        config.getJsonObject('startup').getJsonObject('vx').getMap().each { k, v ->
            println("${k}:${v}")
            def nv = new VXVerticle(vertx,new DeploymentOptions([config:config]), k )
            def nt = new dependentTask(nv,new JsonObject(v as String).getJsonArray('deps').getList())
            nt.start({ result ->
                println("Started ${result}")
            })
        }

    }
}
