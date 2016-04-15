package net.iowntheinter.vertx.componentRegister.componentType.impl

import com.englishtown.promises.Promise
import com.englishtown.promises.When
import com.englishtown.promises.WhenFactory
import com.englishtown.vertx.promises.impl.VertxExecutor
import io.vertx.core.AsyncResult
import io.vertx.core.DeploymentOptions
import io.vertx.core.Handler
import io.vertx.core.Vertx
import net.iowntheinter.vertx.componentRegister.componentType.component

/**
 * Created by grant on 4/10/16.
 */
class VXVerticle implements component {
    Vertx vertx
    When when
    VertxExecutor executor
    String ImplementationID  // example com.this.that or js:myVerticle.js
    String name //human readable name of this instance
    UUID id; //launch
    DeploymentOptions ops
    VXVerticle(Vertx vertx, DeploymentOptions opts, String ImplementationID) {
        this.executor= new VertxExecutor(vertx);
        this.when = WhenFactory.createFor({ executor })
        this.vertx = vertx
        this.ImplementationID = ImplementationID
        this.ops = opts
    }






    Promise deploy(){

    }
    Closure after = { AsyncResult res ->
        Closure nxtcb = {};
        this.id = res.result()
        nxtcb(res)
    }

    @Override
    void start(String name, Handler<AsyncResult> cb) {
        this.name = name

        vertx.deployVerticle(ImplementationID, cb)
    }

    @Override
    void stop(String id, Handler<AsyncResult> cb) {
        vertx.undeploy(id,
                after.setProperty('nxtcb', cb)// what? are you serious?
        )
    }
}
