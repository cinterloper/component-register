package net.iowntheinter.coreLauncher

import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.json.JsonObject

/**
 * Created by g on 10/22/16.
 */
interface componentLauncher {
    void launchGroup(JsonObject groupDesc, Handler<AsyncResult<JsonObject>> resultHandler)
    void launchContainer(JsonObject containerDesc, Handler<AsyncResult<JsonObject>> resultHandler)
    void deployVertxComponent(JsonObject componentDesc, Handler<AsyncResult<JsonObject>> resultHandler)

}
