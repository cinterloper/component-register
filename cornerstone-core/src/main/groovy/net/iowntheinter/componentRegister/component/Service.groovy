package net.iowntheinter.componentRegister.component

import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.json.JsonObject

/**
 * Created by g on 11/21/16.
 */
interface Service {
    void start(Handler<AsyncResult<JsonObject>> cb) //wakeup cb after its actually started
    void stop(Handler<AsyncResult<JsonObject>> cb)
    String getCapabilityTag()
    String getName()
    Map<String,String> getEndpoints()
}
