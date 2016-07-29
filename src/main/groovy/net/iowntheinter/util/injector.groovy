package net.iowntheinter.util

import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject

/**
 * Created by g on 7/25/16.
 */

interface injector {
    /**
     * an injector is called right before a component is started, and is passed the components configuration
     * the injector can do arbitrary work, then return a Set of enviornment variables to add to the components
     * startup enviornment
     * @param componentcfg
     * @param vertx
     * @return
     */
    Set inject(JsonObject componentcfg, Vertx vertx)
}
