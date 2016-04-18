package net.iowntheinter.vertx.configurationManager

import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.json.JsonObject

/**
 * Created by grant on 4/15/16.
 */
interface configurationStore {
    boolean registerComponent(String Name)
    boolean unregisterComponent(String Name)
    void setConfig(String componentName, JsonObject cfg, Handler<AsyncResult> cb)
    void removeConfig(String componentName, Handler<AsyncResult> cb)
    void getConfig(String componentName, Handler<AsyncResult> cb)
}
