package net.iowntheinter.vertx.componentRegister.impl;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.Vertx
/*
 * @author <a href="http://tfox.org">Tim Fox</a>
 */

public class CoreLauncher extends AbstractVerticle {

    Vertx vertx;


    public static void main(String[] args) {
        def c = this.newInstance();
        c.vertx = Vertx.vertx()
        c.start();
    }

    CoreLauncher() {
        println("hello")
    }

    public void final_shutdown(String topic, String value) {
        vertx.close()
    }
    public void start_manager(){}


    @Override
    public void start() throws Exception {
        println(vertx)
        start_manager()
    }
}
