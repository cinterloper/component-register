package net.iowntheinter.componentRegister.component

import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.json.JsonObject

/**
 * Created by g on 11/21/16.
 */
//in most cases this will be another Cornerstone instance, or maybe another JVM Vert.x cluster member
interface Peer {
    void getCapabilities(Handler<AsyncResult<JsonObject>> h)
    void getUptime(Handler<AsyncResult<JsonObject>> h)
    void directMessage(Handler<AsyncResult<JsonObject>> h)
}
