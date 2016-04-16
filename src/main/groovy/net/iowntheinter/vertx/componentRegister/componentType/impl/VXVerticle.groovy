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
    String ImplementationID  // example com.this.that or js:myVerticle.js
    String name //human readable name of this instance
    UUID id; //launch
    DeploymentOptions ops
    Map deps =[:]
    String tasktype

    VXVerticle(Vertx vertx, DeploymentOptions opts, String ImplementationID) {
        this.vertx = vertx
        this.ImplementationID = ImplementationID
        this.ops = opts
        deps =opts.config.getJsonObject()
    }


    @Override
    void start(Handler<AsyncResult> cb) {
        this.name = name
        vertx.deployVerticle(ImplementationID, cb)
    }

    @Override
    void stop( Handler<AsyncResult> cb) {
        vertx.undeploy(id as String, cb)
    }

    @Override
    void registrationEvent(Map peerNotification, Handler<AsyncResult> cb) {
     //send a message to the verticles personal channel
    }



    private void listen() {
        def eb = vertx.eventBus()
        def depchdl = eb.consumer("conerstone:deployments")
        depchdl.handler({ msg ->
            this.notify(msg.body() as Map, this.startCb as Handler)
            if((msg.body() as Map).get("id") == this.id){
                started = true
                this.runCb()
            }
        })
    }
}
