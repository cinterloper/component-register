package net.iowntheinter.vertx.componentRegister.componentType

import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.json.JsonObject

/**
 * Created by grant on 4/13/16.
 * every component should have a start SLATimeout
 * it must check in by that time or will be considered failed
 */
interface component {
    void start(String name, Handler<AsyncResult> cb)

    void stop(String id, Handler<AsyncResult> cb)

}
