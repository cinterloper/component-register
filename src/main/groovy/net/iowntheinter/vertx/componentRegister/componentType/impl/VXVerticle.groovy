package net.iowntheinter.vertx.componentRegister.componentType.impl

import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.Vertx
import net.iowntheinter.vertx.componentRegister.componentType.component

/**
 * Created by grant on 4/10/16.
 */
class VXVerticle implements component {
    Vertx vertx
    String ImplementationID  // example com.this.that or js:myVerticle.js
    String name //human readable name of this instance
    UUID id; //launch
    VXVerticle(Vertx vertx, String ImplementationID) {
        this.vertx = vertx
        this.ImplementationID = ImplementationID
    }

    @Override
    void start(String name, Handler<AsyncResult> cb) {
        this.name = name
        vertx.deployVerticle(ImplementationID,cb )
    }

    @Override
    void stop(String id, Handler<AsyncResult> cb) {
        vertx.undeploy(id,cb)
    }
}
