package net.iowntheinter.util.configuration.store

import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.json.JsonObject

/**
 * Created by grant on 4/15/16.
 */
interface configStore {
    void init(Map cfg, Closure cb)
    void setConfig(String path, JsonObject cfg, Closure cb)
    void removeConfig(String path, Closure cb)
    void getConfig(String path, Closure cb)
    void close(Closure cb)
}
